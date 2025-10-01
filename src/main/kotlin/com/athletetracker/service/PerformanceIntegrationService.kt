package com.athletetracker.service

import com.athletetracker.entity.*
import com.athletetracker.repository.PerformanceMetricRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service responsible for integrating performance data across assessments, workouts, and metrics.
 * Handles automatic generation of performance metrics from various sources.
 */
@Service
@Transactional
class PerformanceIntegrationService(
    private val performanceMetricRepository: PerformanceMetricRepository
) {
    
    /**
     * Creates a performance metric from an assessment result
     */
    fun createMetricFromAssessment(
        assessmentResult: AssessmentResult,
        metricType: MetricType
    ): PerformanceMetric? {
        // Only create if the assessment is configured to generate metrics
        if (!assessmentResult.assessment.generatePerformanceMetric) {
            return null
        }
        
        // Check if this assessment result already has a metric
        val existing = performanceMetricRepository.findBySourceAssessmentResult(assessmentResult)
        if (existing != null) {
            return existing
        }
        
        val metric = PerformanceMetric(
            athlete = assessmentResult.athlete,
            metricType = metricType,
            metricValue = assessmentResult.value,
            unit = assessmentResult.assessment.unit,
            testDate = assessmentResult.testDate.atStartOfDay(),
            notes = "Generated from assessment: ${assessmentResult.assessment.name}",
            recordedBy = assessmentResult.conductedBy,
            sourceType = PerformanceMetricSource.ASSESSMENT,
            sourceAssessmentResult = assessmentResult,
            isPersonalRecord = assessmentResult.isBaseline || isNewPersonalRecord(
                assessmentResult.athlete,
                metricType,
                assessmentResult.value
            )
        )
        
        return performanceMetricRepository.save(metric)
    }
    
    /**
     * Creates a performance metric from a workout exercise PR
     */
    fun createMetricFromWorkoutPR(
        athleteWorkoutExercise: AthleteWorkoutExercise,
        metricType: MetricType,
        metricValue: Double,
        unit: String
    ): PerformanceMetric? {
        // Check if this workout exercise already has a metric
        val existing = performanceMetricRepository.findBySourceWorkoutExercise(athleteWorkoutExercise)
        if (existing != null) {
            return existing
        }
        
        val metric = PerformanceMetric(
            athlete = athleteWorkoutExercise.athleteWorkout.athlete,
            metricType = metricType,
            metricValue = metricValue,
            unit = unit,
            testDate = athleteWorkoutExercise.athleteWorkout.workoutDate,
            notes = "Generated from workout PR: ${athleteWorkoutExercise.exercise.name}",
            recordedBy = athleteWorkoutExercise.athleteWorkout.coach,
            sourceType = PerformanceMetricSource.WORKOUT_PR,
            sourceWorkoutExercise = athleteWorkoutExercise,
            isPersonalRecord = true // By definition, if we're creating from a PR, it's a personal record
        )
        
        return performanceMetricRepository.save(metric)
    }
    
    /**
     * Maps assessment types to performance metric types
     */
    fun mapAssessmentToMetricType(assessment: Assessment): MetricType? {
        return when {
            assessment.name.contains("Bench Press", ignoreCase = true) && 
            assessment.type == AssessmentType.WEIGHT -> MetricType.BENCH_PRESS_MAX
            
            assessment.name.contains("Squat", ignoreCase = true) && 
            assessment.type == AssessmentType.WEIGHT -> MetricType.SQUAT_MAX
            
            assessment.name.contains("Deadlift", ignoreCase = true) && 
            assessment.type == AssessmentType.WEIGHT -> MetricType.DEADLIFT_MAX
            
            assessment.name.contains("Vertical Jump", ignoreCase = true) -> MetricType.VERTICAL_JUMP
            
            assessment.name.contains("Broad Jump", ignoreCase = true) -> MetricType.BROAD_JUMP
            
            assessment.name.contains("40", ignoreCase = true) && 
            assessment.name.contains("yard", ignoreCase = true) -> MetricType.FORTY_YARD_DASH
            
            assessment.name.contains("Mile", ignoreCase = true) && 
            assessment.type == AssessmentType.TIMED -> MetricType.MILE_TIME
            
            assessment.name.contains("Body Weight", ignoreCase = true) -> MetricType.BODY_WEIGHT
            
            assessment.name.contains("Body Fat", ignoreCase = true) -> MetricType.BODY_FAT_PERCENTAGE
            
            // Add more mappings as needed
            else -> null
        }
    }
    
    /**
     * Maps exercise names to performance metric types for workout PRs
     */
    fun mapExerciseToMetricType(exercise: Exercise): MetricType? {
        return when {
            exercise.name.contains("Bench Press", ignoreCase = true) -> MetricType.BENCH_PRESS_MAX
            exercise.name.contains("Squat", ignoreCase = true) -> MetricType.SQUAT_MAX
            exercise.name.contains("Deadlift", ignoreCase = true) -> MetricType.DEADLIFT_MAX
            exercise.name.contains("Overhead Press", ignoreCase = true) -> MetricType.OVERHEAD_PRESS_MAX
            // Add more mappings as needed
            else -> null
        }
    }
    
    /**
     * Checks if a value represents a new personal record for an athlete and metric type
     */
    private fun isNewPersonalRecord(
        athlete: Athlete,
        metricType: MetricType,
        newValue: Double
    ): Boolean {
        val currentBest = performanceMetricRepository.findLatestByAthleteAndMetricType(athlete, metricType)
        return if (currentBest == null) {
            true // First record is always a PR
        } else {
            // For most metrics, higher is better. Time-based metrics are lower is better.
            when (metricType) {
                MetricType.FORTY_YARD_DASH, 
                MetricType.TWENTY_YARD_DASH, 
                MetricType.TEN_YARD_DASH,
                MetricType.MILE_TIME,
                MetricType.HALF_MILE_TIME,
                MetricType.SHUTTLE_RUN,
                MetricType.THREE_CONE_DRILL -> newValue < currentBest.metricValue
                else -> newValue > currentBest.metricValue
            }
        }
    }
}