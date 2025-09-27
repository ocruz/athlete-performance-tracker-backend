package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.Athlete
import com.athletetracker.entity.AthleteProgram
import com.athletetracker.entity.Program
import com.athletetracker.entity.ProgramStatus
import com.athletetracker.entity.User
import com.athletetracker.service.AthleteProgramService
import com.athletetracker.service.AthleteService
import com.athletetracker.service.ProgramService
import com.athletetracker.service.CoachService
import com.athletetracker.service.UserProfileService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/coach")
@PreAuthorize("hasRole('COACH')")
class CoachController(
    private val athleteProgramService: AthleteProgramService,
    private val athleteService: AthleteService,
    private val programService: ProgramService,
    private val coachService: CoachService,
    userProfileService: UserProfileService
) : BaseProfileController(userProfileService) {

    @GetMapping("/profile")
    fun getCoachProfile(authentication: Authentication): ResponseEntity<CompleteProfileDto.CoachProfile> {
        val userId = getUserIdFromAuthentication(authentication)
        val user = getUserFromAuthentication(authentication)
        val userProfile = userProfileService.getUserProfile(userId)
        val coach = coachService.getCoachByUserId(userId)
        
        val userDto = user.toDto()
        val completeProfile = CompleteProfileDto.forCoach(userDto, coach, userProfile)
        return ResponseEntity.ok(completeProfile)
    }

    /**
     * Update coach-specific information
     */
    @PutMapping("/profile")
    fun updateCoachProfile(
        @Valid @RequestBody updateRequest: UpdateCoachRequest,
        authentication: Authentication
    ): ResponseEntity<CoachDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val updatedCoach = coachService.updateCoach(userId, updateRequest)
        return ResponseEntity.ok(updatedCoach)
    }

    /**
     * Upload coach profile photo (overrides base implementation to also update coach entity)
     */
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
        
        // Update photo in both user profile and coach profile
        userProfileService.updateProfilePhoto(userId, photoUrl)
        val updatedCoach = coachService.updateCoachPhoto(userId, photoUrl)
        
        return ResponseEntity.ok(updatedCoach)
    }

    // ===== COACH DASHBOARD AND MANAGEMENT ENDPOINTS =====

    @GetMapping("/dashboard")
    fun getCoachDashboard(authentication: Authentication): ResponseEntity<CoachDashboardResponse> {
        val user = authentication.principal as User
        val coachId = user.id
        
        val managedAthletes = athleteService.getAllAthletes() // TODO: Filter by coach
        val assignedPrograms = athleteProgramService.getProgramsByCoach(coachId)
        val createdPrograms = programService.getProgramsByCreator(coachId)
        
        val dashboardData = CoachDashboardResponse(
            coach = CoachBasicInfo(
                id = coachId,
                name = user.fullName,
                email = user.email
            ),
            managedAthletes = managedAthletes.map { athlete ->
                AthleteOverview(
                    id = athlete.id,
                    name = "${athlete.firstName} ${athlete.lastName}",
                    sport = athlete.sport.name,
                    activePrograms = 0, // TODO: Calculate
                    lastActivity = null // TODO: Get last activity
                )
            },
            recentAssignments = assignedPrograms.take(5).map { assignment ->
                RecentAssignment(
                    id = assignment.id,
                    athleteName = "${assignment.athlete.firstName} ${assignment.athlete.lastName}",
                    programName = assignment.program.name,
                    assignedDate = assignment.createdAt.toLocalDate(),
                    status = assignment.status.name
                )
            },
            programStats = ProgramStats(
                totalPrograms = createdPrograms.size,
                activeAssignments = assignedPrograms.count { it.status.name == "ACTIVE" },
                completionRate = 0.0 // TODO: Calculate
            )
        )
        
        return ResponseEntity.ok(dashboardData)
    }

    @GetMapping("/athletes")
    fun getManagedAthletes(authentication: Authentication): ResponseEntity<List<AthleteWithPrograms>> {
        val user = authentication.principal as User

        val athletes = athleteService.getAllAthletes() // TODO: Filter by coach
        val athletesWithPrograms = athletes.map { athlete ->
            val programs = athleteProgramService.getAthletePrograms(athlete.id)
            AthleteWithPrograms(
                athlete = AthleteBasicDto(
                    id = athlete.id,
                    firstName = athlete.firstName,
                    lastName = athlete.lastName,
                    dateOfBirth = athlete.dateOfBirth,
                    sport = athlete.sport.name
                ),
                assignedPrograms = programs,
                progressSummary = calculateProgressSummary(programs)
            )
        }
        
        return ResponseEntity.ok(athletesWithPrograms)
    }

    @PostMapping("/programs/assign")
    fun assignProgram(
        @RequestBody request: AssignProgramRequest,
        authentication: Authentication
    ): ResponseEntity<AthleteProgramResponse> {
        val user = authentication.principal as User
        val assignment = athleteProgramService.assignProgram(request, user.id)
        return ResponseEntity.ok(assignment)
    }


    @GetMapping("/analytics/team")
    fun getTeamAnalytics(authentication: Authentication): ResponseEntity<TeamAnalyticsResponse> {
        val user = authentication.principal as User
        val coachId = user.id
        
        val assignedPrograms = athleteProgramService.getProgramsByCoach(coachId)
        
        val analytics = TeamAnalyticsResponse(
            totalAthletes = 0, // TODO: Calculate
            totalPrograms = assignedPrograms.size,
            averageCompletion = 0.0, // TODO: Calculate
            topPerformers = emptyList(), // TODO: Calculate
            programEffectiveness = emptyList() // TODO: Calculate
        )
        
        return ResponseEntity.ok(analytics)
    }

    @GetMapping("/programs")
    fun getMyPrograms(authentication: Authentication): ResponseEntity<List<Program>> {
        val user = authentication.principal as User
        val coachId = user.id
        
        val programs = programService.getProgramsByCreator(coachId)
        return ResponseEntity.ok(programs)
    }

    private fun calculateProgressSummary(programs: List<AthleteProgramResponse>): AthleteProgressSummary {
        return AthleteProgressSummary(
            totalPrograms = programs.size,
            activePrograms = programs.count { it.status == ProgramStatus.ACTIVE },
            completedPrograms = programs.count { it.status == ProgramStatus.COMPLETED },
            averageCompletion = programs.map { it.progressSummary.completionPercentage }.average().takeIf { !it.isNaN() } ?: 0.0
        )
    }
}

// Response DTOs for coach endpoints
data class CoachDashboardResponse(
    val coach: CoachBasicInfo,
    val managedAthletes: List<AthleteOverview>,
    val recentAssignments: List<RecentAssignment>,
    val programStats: ProgramStats
)

data class CoachBasicInfo(
    val id: Long,
    val name: String,
    val email: String
)

data class AthleteOverview(
    val id: Long,
    val name: String,
    val sport: String,
    val activePrograms: Int,
    val lastActivity: java.time.LocalDate?
)

data class RecentAssignment(
    val id: Long,
    val athleteName: String,
    val programName: String,
    val assignedDate: java.time.LocalDate,
    val status: String
)

data class ProgramStats(
    val totalPrograms: Int,
    val activeAssignments: Int,
    val completionRate: Double
)

data class AthleteWithPrograms(
    val athlete: AthleteBasicDto,
    val assignedPrograms: List<AthleteProgramResponse>,
    val progressSummary: AthleteProgressSummary
)

data class AthleteProgressSummary(
    val totalPrograms: Int,
    val activePrograms: Int,
    val completedPrograms: Int,
    val averageCompletion: Double
)

data class TeamAnalyticsResponse(
    val totalAthletes: Int,
    val totalPrograms: Int,
    val averageCompletion: Double,
    val topPerformers: List<AthletePerformance>,
    val programEffectiveness: List<ProgramEffectiveness>
)

data class AthletePerformance(
    val athleteId: Long,
    val athleteName: String,
    val completionRate: Double,
    val averageRPE: Double
)

data class ProgramEffectiveness(
    val programId: Long,
    val programName: String,
    val completionRate: Double,
    val satisfactionScore: Double
)