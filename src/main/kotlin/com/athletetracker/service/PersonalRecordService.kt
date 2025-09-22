package com.athletetracker.service

import com.athletetracker.entity.*
import com.athletetracker.repository.PerformanceMetricRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service responsible for detecting and managing personal records across workouts and assessments.
 */
@Service
@Transactional
class PersonalRecordService(
    private val performanceMetricRepository: PerformanceMetricRepository,
    private val performanceIntegrationService: PerformanceIntegrationService
) {
    
    /**
     * Detects if a workout exercise represents a new personal record
     */
    fun detectWorkoutPR(workoutExercise: WorkoutExercise): PRDetectionResult {
        // Only check for strength exercises with weight data
        if (workoutExercise.actualWeight == null || workoutExercise.actualReps == null) {
            return PRDetectionResult(false, null, null, null)
        }
        
        val metricType = performanceIntegrationService.mapExerciseToMetricType(workoutExercise.exercise)
            ?: return PRDetectionResult(false, null, null, null)
        
        val athlete = workoutExercise.workout.athlete
        val newWeight = workoutExercise.actualWeight
        
        // Get current best for this exercise
        val currentBest = performanceMetricRepository.findLatestByAthleteAndMetricType(athlete, metricType)
        
        val isPR = when {
            currentBest == null -> true // First record is always a PR
            newWeight > currentBest.metricValue -> true // Higher weight is better
            else -> false
        }
        
        return if (isPR) {
            PRDetectionResult(
                isPR = true,
                metricType = metricType,
                newValue = newWeight,
                previousValue = currentBest?.metricValue,
                unit = "lbs"
            )
        } else {
            PRDetectionResult(false, null, null, null)
        }
    }
    
    /**
     * Calculates 1-rep max equivalent for tracking strength PRs
     * Uses the Brzycki formula: weight / (1.0278 - 0.0278 * reps)
     */
    fun calculateOneRepMaxEquivalent(weight: Double, reps: Int): Double {
        return if (reps == 1) {
            weight
        } else {
            weight / (1.0278 - 0.0278 * reps)
        }
    }
    
    /**
     * Detects PRs based on 1RM equivalents rather than just raw weight
     */
    fun detectWorkoutPRWithOneRepMax(workoutExercise: WorkoutExercise): PRDetectionResult {
        if (workoutExercise.actualWeight == null || workoutExercise.actualReps == null) {
            return PRDetectionResult(false, null, null, null)
        }
        
        val metricType = performanceIntegrationService.mapExerciseToMetricType(workoutExercise.exercise)
            ?: return PRDetectionResult(false, null, null, null)
        
        val athlete = workoutExercise.workout.athlete
        val newOneRM = calculateOneRepMaxEquivalent(
            workoutExercise.actualWeight!!,
            workoutExercise.actualReps!!
        )
        
        // Get current best 1RM
        val currentBest = performanceMetricRepository.findLatestByAthleteAndMetricType(athlete, metricType)
        
        val isPR = when {
            currentBest == null -> true
            newOneRM > currentBest.metricValue -> true
            else -> false
        }
        
        return if (isPR) {
            PRDetectionResult(
                isPR = true,
                metricType = metricType,
                newValue = newOneRM,
                previousValue = currentBest?.metricValue,
                unit = "lbs (1RM equivalent)"
            )
        } else {
            PRDetectionResult(false, null, null, null)
        }
    }
    
    /**
     * Gets all personal records for an athlete
     */
    @Transactional(readOnly = true)
    fun getPersonalRecords(athlete: Athlete): List<PerformanceMetric> {
        return performanceMetricRepository.findPersonalRecordsByAthlete(athlete)
    }
    
    /**
     * Gets PR timeline for an athlete showing progression over time
     */
    @Transactional(readOnly = true)
    fun getPRTimeline(athlete: Athlete): List<PRTimelineEntry> {
        val allPRs = getPersonalRecords(athlete)
        
        return allPRs.map { metric ->
            PRTimelineEntry(
                id = metric.id,
                metricType = metric.metricType,
                value = metric.metricValue,
                unit = metric.unit ?: "",
                date = metric.testDate,
                source = metric.sourceType,
                exerciseName = when (metric.sourceType) {
                    PerformanceMetricSource.WORKOUT_PR -> metric.sourceWorkoutExercise?.exercise?.name
                    PerformanceMetricSource.ASSESSMENT -> metric.sourceAssessmentResult?.assessment?.name
                    else -> null
                },
                notes = metric.notes
            )
        }.sortedByDescending { it.date }
    }
}

/**
 * Result of PR detection
 */
data class PRDetectionResult(
    val isPR: Boolean,
    val metricType: MetricType?,
    val newValue: Double?,
    val previousValue: Double?,
    val unit: String? = null
)

/**
 * Entry in the PR timeline
 */
data class PRTimelineEntry(
    val id: Long,
    val metricType: MetricType,
    val value: Double,
    val unit: String,
    val date: java.time.LocalDateTime,
    val source: PerformanceMetricSource,
    val exerciseName: String?,
    val notes: String?
)