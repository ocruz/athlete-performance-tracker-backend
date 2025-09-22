package com.athletetracker.repository

import com.athletetracker.entity.Athlete
import com.athletetracker.entity.MetricType
import com.athletetracker.entity.PerformanceMetric
import com.athletetracker.entity.AssessmentResult
import com.athletetracker.entity.WorkoutExercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PerformanceMetricRepository : JpaRepository<PerformanceMetric, Long> {
    fun findByAthleteOrderByTestDateDesc(athlete: Athlete): List<PerformanceMetric>
    fun findByAthleteAndMetricTypeOrderByTestDateDesc(athlete: Athlete, metricType: MetricType): List<PerformanceMetric>
    
    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.athlete = :athlete AND " +
           "pm.metricType = :metricType AND pm.testDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pm.testDate DESC")
    fun findByAthleteAndMetricTypeAndDateRange(
        @Param("athlete") athlete: Athlete,
        @Param("metricType") metricType: MetricType,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<PerformanceMetric>
    
    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.athlete = :athlete AND " +
           "pm.metricType = :metricType ORDER BY pm.testDate DESC LIMIT 1")
    fun findLatestByAthleteAndMetricType(
        @Param("athlete") athlete: Athlete,
        @Param("metricType") metricType: MetricType
    ): PerformanceMetric?
    
    fun findBySourceAssessmentResult(assessmentResult: AssessmentResult): PerformanceMetric?
    
    fun findBySourceWorkoutExercise(workoutExercise: WorkoutExercise): PerformanceMetric?
    
    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.athlete = :athlete AND pm.isPersonalRecord = true ORDER BY pm.testDate DESC")
    fun findPersonalRecordsByAthlete(@Param("athlete") athlete: Athlete): List<PerformanceMetric>
    
    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.athlete = :athlete ORDER BY pm.testDate DESC")
    fun findUnifiedPerformanceMetrics(@Param("athlete") athlete: Athlete): List<PerformanceMetric>
}