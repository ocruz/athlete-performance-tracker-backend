package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.Sport
import com.athletetracker.entity.User
import com.athletetracker.service.AthleteService
import com.athletetracker.service.AthleteProgramService
import com.athletetracker.service.AthleteDashboardService
import com.athletetracker.service.AthleteProgramWorkoutService
import com.athletetracker.service.UserProfileService
import com.athletetracker.repository.AthleteRepository
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class AthleteManagementController(
    private val athleteService: AthleteService,
    private val athleteProgramService: AthleteProgramService,
    private val athleteDashboardService: AthleteDashboardService,
    private val athleteProgramWorkoutService: AthleteProgramWorkoutService,
    private val athleteRepository: AthleteRepository,
    userProfileService: UserProfileService,
    private val invitationService: com.athletetracker.service.InvitationService
) : BaseProfileController(userProfileService) {

    // ===== ADMIN/COACH ATHLETE MANAGEMENT ENDPOINTS (/athletes/*) =====

    @GetMapping("/athletes")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun getAllAthletes(authentication: Authentication): ResponseEntity<List<AthleteDto>> {
        val athletes = athleteService.getAllAthletes()
        return ResponseEntity.ok(athletes)
    }

    @GetMapping("/athletes/{id}")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun getAthleteById(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<AthleteDto> {
        val athlete = athleteService.getAthleteById(id)
        return ResponseEntity.ok(athlete)
    }

    @PostMapping("/athletes")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun createAthlete(
        @RequestBody request: CreateAthleteRequest,
        authentication: Authentication
    ): ResponseEntity<AthleteDto> {
        val createdAthlete = athleteService.createAthlete(request)
        
        // Send invitation if email is provided
        if (!request.email.isNullOrBlank()) {
            try {
                val userId = authentication.name?.toLongOrNull() // Get coach user ID if available
                invitationService.createInvitation(createdAthlete.id, request.email, userId)
                println("✅ Invitation sent successfully to ${request.email}")
            } catch (e: Exception) {
                println("⚠️  Failed to send invitation to ${request.email}: ${e.message}")
                // Don't fail the athlete creation if invitation fails
            }
        }
        
        return ResponseEntity.ok(createdAthlete)
    }

    @PutMapping("/athletes/{id}")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun updateAthlete(
        @PathVariable id: Long,
        @RequestBody request: UpdateAthleteRequest,
        authentication: Authentication
    ): ResponseEntity<AthleteDto> {
        val updatedAthlete = athleteService.updateAthlete(id, request)
        return ResponseEntity.ok(updatedAthlete)
    }

    @DeleteMapping("/athletes/{id}")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun deleteAthlete(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        athleteService.deleteAthlete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/athletes/sports/{sport}")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun getAthletesBySport(
        @PathVariable sport: Sport,
        authentication: Authentication
    ): ResponseEntity<List<AthleteDto>> {
        val athletes = athleteService.getAthletesBySport(sport)
        return ResponseEntity.ok(athletes)
    }

    @PostMapping("/athletes/search")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun searchAthletes(
        @RequestBody searchRequest: AthleteSearchRequest,
        authentication: Authentication
    ): ResponseEntity<List<AthleteDto>> {
        val athletes = athleteService.searchAthletes(searchRequest)
        return ResponseEntity.ok(athletes)
    }

    @GetMapping("/athletes/stats")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun getAthleteStats(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        val stats = athleteService.getAthleteStats()
        return ResponseEntity.ok(stats)
    }

    @PostMapping("/athletes/{id}/resend-invitation")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun resendInvitation(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ResendInvitationResponse> {
        return try {
            val userId = authentication.name?.toLongOrNull()
            invitationService.resendInvitation(id, userId)
            
            // Check if this was a resend of existing invitation or creation of new one
            val isReminder = invitationService.getInvitationStatus(id) == com.athletetracker.service.InvitationStatus.INVITATION_SENT
            
            val response = ResendInvitationResponse(
                success = true,
                message = if (isReminder) "Invitation reminder sent successfully" else "New invitation sent successfully",
                isReminder = isReminder
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            val response = ResendInvitationResponse(
                success = false,
                message = e.message ?: "Failed to resend invitation"
            )
            ResponseEntity.badRequest().body(response)
        } catch (e: Exception) {
            val response = ResendInvitationResponse(
                success = false,
                message = "An error occurred while resending the invitation"
            )
            ResponseEntity.internalServerError().body(response)
        }
    }

    @GetMapping("/athletes/{id}/invitation-status")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun getInvitationStatus(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val status = invitationService.getInvitationStatus(id)
            val invitations = invitationService.getInvitationsForAthlete(id)
            
            val response = mutableMapOf<String, Any>(
                "status" to status.name,
                "statusDescription" to when (status) {
                    com.athletetracker.service.InvitationStatus.NO_EMAIL -> "No email address"
                    com.athletetracker.service.InvitationStatus.NO_INVITATION -> "No invitation sent"
                    com.athletetracker.service.InvitationStatus.INVITATION_SENT -> "Invitation pending"
                    com.athletetracker.service.InvitationStatus.INVITATION_EXPIRED -> "Invitation expired"
                    com.athletetracker.service.InvitationStatus.INVITATION_USED -> "Invitation used"
                    com.athletetracker.service.InvitationStatus.ACCOUNT_CREATED -> "Account created"
                },
                "canResend" to (status in listOf(
                    com.athletetracker.service.InvitationStatus.NO_INVITATION,
                    com.athletetracker.service.InvitationStatus.INVITATION_EXPIRED,
                    com.athletetracker.service.InvitationStatus.INVITATION_USED,
                    com.athletetracker.service.InvitationStatus.INVITATION_SENT
                )),
                "invitationCount" to invitations.size
            )
            
            // Add lastInvitationDate only if it exists
            invitations.firstOrNull()?.createdAt?.let {
                response["lastInvitationDate"] = it
            }
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            val errorResponse: Map<String, Any> = mapOf("error" to (e.message ?: "Athlete not found"))
            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // ===== ATHLETE SELF-SERVICE ENDPOINTS (/athlete/*) =====

    @GetMapping("/athlete/profile")
    @PreAuthorize("hasRole('ATHLETE')")
    fun getAthleteProfile(authentication: Authentication): ResponseEntity<CompleteProfileDto.AthleteProfile> {
        val userId = getUserIdFromAuthentication(authentication)
        val user = getUserFromAuthentication(authentication)
        val userProfile = userProfileService.getUserProfile(userId)
        val athlete = athleteService.getAthleteByUserId(userId)
        
        val userDto = user.toDto()
        val completeProfile = CompleteProfileDto.forAthlete(userDto, athlete, userProfile)
        return ResponseEntity.ok(completeProfile)
    }

    @PutMapping("/athlete/profile")
    @PreAuthorize("hasRole('ATHLETE')")
    fun updateAthleteProfile(
        @Valid @RequestBody updateRequest: UpdateAthleteRequest,
        authentication: Authentication
    ): ResponseEntity<AthleteDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val updatedAthlete = athleteService.updateAthleteByUserId(userId, updateRequest)
        return ResponseEntity.ok(updatedAthlete)
    }

    @PostMapping("/athlete/profile/photo")
    @PreAuthorize("hasRole('ATHLETE')")
    override fun uploadProfilePhoto(
        @RequestParam("file") file: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<*> {
        val userId = getUserIdFromAuthentication(authentication)
        
        // Basic file validation
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(mapOf("error" to "File is required"))
        }
        
        if (!isValidImageFile(file)) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid image file"))
        }
        
        val photoUrl = "/uploads/profiles/${userId}_${file.originalFilename}"
        
        // Update photo in both user profile and athlete profile
        userProfileService.updateProfilePhoto(userId, photoUrl)
        val updatedAthlete = athleteService.updateAthletePhoto(userId, photoUrl)
        
        return ResponseEntity.ok(updatedAthlete)
    }

    @GetMapping("/athlete/dashboard")
    @PreAuthorize("hasRole('ATHLETE')")
    fun getDashboard(authentication: Authentication): ResponseEntity<AthleteDashboardResponse> {
        val user = authentication.principal as User
        val dashboardData = athleteDashboardService.getDashboardData(user)
        return ResponseEntity.ok(dashboardData)
    }

    @GetMapping("/athlete/programs")
    @PreAuthorize("hasRole('ATHLETE')")
    fun getMyPrograms(authentication: Authentication): ResponseEntity<List<AthleteProgramResponse>> {
        val user = authentication.principal as User
        val athlete = athleteRepository.findByUserId(user.id)
            ?: throw IllegalArgumentException("No athlete profile found for user: ${user.id}")
        
        val programs = athleteProgramService.getAthletePrograms(athlete.id)
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/athlete/programs/active")
    @PreAuthorize("hasRole('ATHLETE')")
    fun getActivePrograms(authentication: Authentication): ResponseEntity<List<AthleteProgramResponse>> {
        val user = authentication.principal as User
        val athlete = athleteRepository.findByUserId(user.id)
            ?: throw IllegalArgumentException("No athlete profile found for user: ${user.id}")
        
        val activePrograms = athleteProgramService.getActiveProgramsForAthlete(athlete.id)
        return ResponseEntity.ok(activePrograms)
    }

    @GetMapping("/athlete/workouts")
    @PreAuthorize("hasRole('ATHLETE')")
    fun getMyWorkouts(authentication: Authentication): ResponseEntity<List<AthleteWorkoutDto>> {
        val user = authentication.principal as User
        val athlete = athleteRepository.findByUserId(user.id)
            ?: throw IllegalArgumentException("No athlete profile found for user: ${user.id}")
        
        val workouts = athleteProgramWorkoutService.getAthleteWorkoutsByAthlete(athlete.id)
        return ResponseEntity.ok(workouts)
    }
}