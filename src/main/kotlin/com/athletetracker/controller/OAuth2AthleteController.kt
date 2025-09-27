package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.User
import com.athletetracker.service.AthleteService
import com.athletetracker.service.PerformanceMetricService
import com.athletetracker.service.PersonalRecordService
import com.athletetracker.service.WorkoutService
import com.athletetracker.service.AssessmentService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

/**
 * OAuth2-specific API endpoints for external applications to access athlete data.
 * These endpoints use scope-based authorization instead of role-based authorization.
 */
@RestController
@RequestMapping("/v1/oauth2")
@CrossOrigin(originPatterns = ["*"]) // More permissive for OAuth2 clients
class OAuth2AthleteController(
    private val athleteService: AthleteService,
    private val performanceMetricService: PerformanceMetricService,
    private val personalRecordService: PersonalRecordService,
    private val workoutService: WorkoutService,
    private val assessmentService: AssessmentService
) {
    
    /**
     * Get the authenticated athlete's basic profile information
     * Requires: athlete:read scope
     */
    @GetMapping("/athletes/me")
    @PreAuthorize("hasAuthority('SCOPE_athlete:read')")
    fun getMyProfile(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<OAuth2AthleteProfileResponse> {
        val athleteId = extractAthleteIdFromJwt(jwt)
        val athlete = athleteService.getAthleteEntityById(athleteId)
        
        val profileResponse = OAuth2AthleteProfileResponse(
            id = athlete.id,
            firstName = athlete.firstName,
            lastName = athlete.lastName,
            sport = athlete.sport.name,
            position = athlete.position,
            dateOfBirth = athlete.dateOfBirth.toString(),
            height = athlete.height,
            weight = athlete.weight,
            // Only include contact info if client has sensitive scope
            email = if (hasScope(jwt, "athlete:contact")) athlete.email else null,
            phone = if (hasScope(jwt, "athlete:contact")) athlete.phone else null
        )
        
        return ResponseEntity.ok(profileResponse)
    }
    
    /**
     * Get the authenticated athlete's performance metrics
     * Requires: performance:read scope
     */
    @GetMapping("/athletes/me/metrics")
    @PreAuthorize("hasAuthority('SCOPE_performance:read')")
    fun getMyMetrics(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<PerformanceMetricResponse>> {
        val athleteId = extractAthleteIdFromJwt(jwt)
        val metrics = performanceMetricService.getMetricsByAthlete(athleteId)
        return ResponseEntity.ok(metrics)
    }
    
    /**
     * Get the authenticated athlete's personal records
     * Requires: performance:read scope
     */
    @GetMapping("/athletes/me/records")
    @PreAuthorize("hasAuthority('SCOPE_performance:read')")
    fun getMyPersonalRecords(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<OAuth2PersonalRecordResponse>> {
        val athleteId = extractAthleteIdFromJwt(jwt)
        val athlete = athleteService.getAthleteEntityById(athleteId)
        val records = personalRecordService.getPersonalRecords(athlete)
        
        val recordResponses = records.map { record ->
            OAuth2PersonalRecordResponse(
                metricType = record.metricType.name,
                value = record.metricValue,
                unit = record.unit ?: "",
                achievedDate = record.testDate.toLocalDate().toString(),
                source = record.sourceType.name
            )
        }
        
        return ResponseEntity.ok(recordResponses)
    }
    
    /**
     * Get the authenticated athlete's workout history
     * Requires: workouts:read scope
     */
    @GetMapping("/athletes/me/workouts")
    @PreAuthorize("hasAuthority('SCOPE_workouts:read')")
    fun getMyWorkouts(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<OAuth2WorkoutResponse>> {
        val athleteId = extractAthleteIdFromJwt(jwt)
        val workouts = workoutService.getWorkoutsByAthleteAsDto(athleteId)
        
        val workoutResponses = workouts.map { workout ->
            OAuth2WorkoutResponse(
                id = workout.id,
                name = workout.name,
                date = workout.workoutDate.toLocalDate().toString(),
                duration = workout.duration,
                rpe = workout.rpe,
                exerciseCount = workout.workoutExercises.size,
                completedExercises = workout.summary.completedExercises,
                notes = workout.notes
            )
        }
        
        return ResponseEntity.ok(workoutResponses)
    }
    
    /**
     * Get detailed information about a specific workout
     * Requires: workouts:read scope
     */
    @GetMapping("/athletes/me/workouts/{workoutId}")
    @PreAuthorize("hasAuthority('SCOPE_workouts:read')")
    fun getMyWorkoutDetail(
        @PathVariable workoutId: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<WorkoutDto> {
        val athleteId = extractAthleteIdFromJwt(jwt)
        val workout = workoutService.getWorkoutById(workoutId)
        
        // Verify that this workout belongs to the authenticated athlete
        if (workout.athlete.id != athleteId) {
            return ResponseEntity.notFound().build()
        }
        
        return ResponseEntity.ok(workout)
    }
    
    /**
     * Get the authenticated athlete's assessment results
     * Requires: assessments:read scope
     */
    @GetMapping("/athletes/me/assessments")
    @PreAuthorize("hasAuthority('SCOPE_assessments:read')")
    fun getMyAssessments(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<OAuth2AssessmentResponse>> {
        val athleteId = extractAthleteIdFromJwt(jwt)
        val assessmentResults = assessmentService.getResultsByAthlete(athleteId)
        
        val assessmentResponses = assessmentResults.map { result ->
            OAuth2AssessmentResponse(
                id = result.id,
                assessmentName = result.assessment.name,
                category = result.assessment.category.name,
                value = result.value,
                unit = result.assessment.unit ?: "",
                testDate = result.testDate.toString(),
                isBaseline = result.isBaseline,
                notes = result.notes
            )
        }
        
        return ResponseEntity.ok(assessmentResponses)
    }
    
    /**
     * Get summary statistics for the authenticated athlete
     * Requires: athlete:read scope
     */
    @GetMapping("/athletes/me/summary")
    @PreAuthorize("hasAuthority('SCOPE_athlete:read')")
    fun getMySummary(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<OAuth2AthleteSummaryResponse> {
        val athleteId = extractAthleteIdFromJwt(jwt)
        val athlete = athleteService.getAthleteEntityById(athleteId)
        
        // Get counts from various services if authorized
        val workoutCount = if (hasScope(jwt, "workouts:read")) {
            workoutService.getWorkoutsByAthlete(athleteId).size
        } else null
        
        val metricsCount = if (hasScope(jwt, "performance:read")) {
            performanceMetricService.getMetricsByAthlete(athleteId).size
        } else null
        
        val assessmentCount = if (hasScope(jwt, "assessments:read")) {
            assessmentService.getResultsByAthlete(athleteId).size
        } else null
        
        val summary = OAuth2AthleteSummaryResponse(
            athleteId = athlete.id,
            fullName = "${athlete.firstName} ${athlete.lastName}",
            sport = athlete.sport.name,
            totalWorkouts = workoutCount,
            totalMetrics = metricsCount,
            totalAssessments = assessmentCount,
            memberSince = athlete.createdAt.toLocalDate().toString()
        )
        
        return ResponseEntity.ok(summary)
    }
    
    /**
     * Extract athlete ID from JWT token
     * Uses the enhanced token structure with athleteId claim
     */
    private fun extractAthleteIdFromJwt(jwt: Jwt): Long {
        // First try to get athleteId from the custom claim
        val athleteId = jwt.getClaimAsString("athleteId")?.toLongOrNull()
        if (athleteId != null) {
            return athleteId
        }
        
        // Fallback: try to get from userId claim and look up athlete
        val userId = jwt.getClaimAsString("userId")?.toLongOrNull()
        if (userId != null) {
            val athlete = athleteService.findAthleteByUserId(userId)
            if (athlete != null) {
                return athlete.id
            }
        }
        
        throw IllegalArgumentException("No valid athlete ID found in token")
    }
    
    /**
     * Check if the JWT contains a specific scope
     */
    private fun hasScope(jwt: Jwt, scope: String): Boolean {
        val scopes = jwt.getClaimAsString("scope")?.split(" ") ?: emptyList()
        return scopes.contains(scope)
    }
}

// OAuth2-specific DTOs for external API responses
data class OAuth2AthleteProfileResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val sport: String,
    val position: String?,
    val dateOfBirth: String,
    val height: Double?,
    val weight: Double?,
    val email: String? = null, // Only included with athlete:contact scope
    val phone: String? = null  // Only included with athlete:contact scope
)

data class OAuth2PersonalRecordResponse(
    val metricType: String,
    val value: Double,
    val unit: String,
    val achievedDate: String,
    val source: String
)

data class OAuth2WorkoutResponse(
    val id: Long,
    val name: String?,
    val date: String,
    val duration: Int?,
    val rpe: Int?,
    val exerciseCount: Int,
    val completedExercises: Int,
    val notes: String?
)

data class OAuth2AssessmentResponse(
    val id: Long,
    val assessmentName: String,
    val category: String,
    val value: Double,
    val unit: String,
    val testDate: String,
    val isBaseline: Boolean,
    val notes: String?
)

data class OAuth2AthleteSummaryResponse(
    val athleteId: Long,
    val fullName: String,
    val sport: String,
    val totalWorkouts: Int?,
    val totalMetrics: Int?,
    val totalAssessments: Int?,
    val memberSince: String
)