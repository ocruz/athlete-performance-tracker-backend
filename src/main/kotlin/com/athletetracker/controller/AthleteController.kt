package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.User
import com.athletetracker.service.AthleteProgramService
import com.athletetracker.service.AthleteDashboardService
import com.athletetracker.service.WorkoutService
import com.athletetracker.service.AthleteService
import com.athletetracker.service.UserProfileService
import com.athletetracker.repository.AthleteRepository
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/athlete")
@PreAuthorize("hasRole('ATHLETE')")
class AthleteController(
    private val athleteProgramService: AthleteProgramService,
    private val athleteDashboardService: AthleteDashboardService,
    private val athleteRepository: AthleteRepository,
    private val workoutService: WorkoutService,
    private val athleteService: AthleteService,
    userProfileService: UserProfileService
) : BaseProfileController(userProfileService) {

    @GetMapping("/profile")
    fun getAthleteProfile(authentication: Authentication): ResponseEntity<CompleteProfileDto.AthleteProfile> {
        val userId = getUserIdFromAuthentication(authentication)
        val user = getUserFromAuthentication(authentication)
        val userProfile = userProfileService.getUserProfile(userId)
        val athlete = athleteService.getAthleteByUserId(userId)
        
        val userDto = user.toDto()
        val completeProfile = CompleteProfileDto.forAthlete(userDto, athlete, userProfile)
        return ResponseEntity.ok(completeProfile)
    }

    @PutMapping("/profile")
    fun updateAthleteProfile(
        @Valid @RequestBody updateRequest: UpdateAthleteRequest,
        authentication: Authentication
    ): ResponseEntity<AthleteDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val updatedAthlete = athleteService.updateAthleteByUserId(userId, updateRequest)
        return ResponseEntity.ok(updatedAthlete)
    }

    @PostMapping("/profile/photo")
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

    // ===== ATHLETE DASHBOARD AND PROGRAM ENDPOINTS =====

    @GetMapping("/dashboard")
    fun getDashboard(authentication: Authentication): ResponseEntity<AthleteDashboardResponse> {
        val user = authentication.principal as User
        val dashboardData = athleteDashboardService.getDashboardData(user)
        return ResponseEntity.ok(dashboardData)
    }

    @GetMapping("/programs")
    fun getMyPrograms(authentication: Authentication): ResponseEntity<List<AthleteProgramResponse>> {
        val user = authentication.principal as User
        val athlete = athleteRepository.findByUserId(user.id)
            ?: throw IllegalArgumentException("No athlete profile found for user: ${user.id}")
        
        val programs = athleteProgramService.getAthletePrograms(athlete.id)
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/programs/active")
    fun getActivePrograms(authentication: Authentication): ResponseEntity<List<AthleteProgramResponse>> {
        val user = authentication.principal as User
        val athlete = athleteRepository.findByUserId(user.id)
            ?: throw IllegalArgumentException("No athlete profile found for user: ${user.id}")
        
        val activePrograms = athleteProgramService.getActiveProgramsForAthlete(athlete.id)
        return ResponseEntity.ok(activePrograms)
    }

    @GetMapping("/workouts")
    fun getMyWorkouts(authentication: Authentication): ResponseEntity<List<WorkoutDto>> {
        val user = authentication.principal as User
        val athlete = athleteRepository.findByUserId(user.id)
            ?: throw IllegalArgumentException("No athlete profile found for user: ${user.id}")
        
        val workouts = workoutService.getWorkoutsByAthleteAsDto(athlete.id)
        return ResponseEntity.ok(workouts)
    }
}