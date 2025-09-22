package com.athletetracker.repository

import com.athletetracker.entity.AssessmentResult
import com.athletetracker.entity.AssessmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface AssessmentResultRepository : JpaRepository<AssessmentResult, Long> {
    
    // Find results by athlete
    @Query("SELECT ar FROM AssessmentResult ar JOIN FETCH ar.assessment JOIN FETCH ar.athlete WHERE ar.athlete.id = :athleteId ORDER BY ar.testDate DESC")
    fun findByAthleteIdOrderByTestDateDesc(@Param("athleteId") athleteId: Long): List<AssessmentResult>
    
    // Find results by assessment
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.assessment.id = :assessmentId ORDER BY ar.testDate DESC")
    fun findByAssessmentIdOrderByTestDateDesc(@Param("assessmentId") assessmentId: Long): List<AssessmentResult>
    
    // Find results by athlete and assessment
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.athlete.id = :athleteId AND ar.assessment.id = :assessmentId ORDER BY ar.testDate DESC")
    fun findByAthleteIdAndAssessmentIdOrderByTestDateDesc(
        @Param("athleteId") athleteId: Long, 
        @Param("assessmentId") assessmentId: Long
    ): List<AssessmentResult>
    
    // Find baseline results for athlete
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.athlete.id = :athleteId AND ar.isBaseline = true")
    fun findBaselineResultsByAthleteId(@Param("athleteId") athleteId: Long): List<AssessmentResult>
    
    // Find latest result for athlete and assessment
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.athlete.id = :athleteId AND ar.assessment.id = :assessmentId " +
           "AND ar.status = 'COMPLETED' ORDER BY ar.testDate DESC LIMIT 1")
    fun findLatestResultByAthleteAndAssessment(
        @Param("athleteId") athleteId: Long, 
        @Param("assessmentId") assessmentId: Long
    ): AssessmentResult?
    
    // Find results in date range
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.athlete.id = :athleteId AND " +
           "ar.testDate BETWEEN :startDate AND :endDate ORDER BY ar.testDate DESC")
    fun findByAthleteIdAndDateRange(
        @Param("athleteId") athleteId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<AssessmentResult>
    
    // Find results by status
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.status = :status ORDER BY ar.testDate DESC")
    fun findByStatus(@Param("status") status: AssessmentStatus): List<AssessmentResult>
    
    // Find recent results for athlete (last 30 days)
    @Query("SELECT ar FROM AssessmentResult ar JOIN FETCH ar.assessment JOIN FETCH ar.athlete WHERE ar.athlete.id = :athleteId AND " +
           "ar.testDate >= :since AND ar.status = 'COMPLETED' ORDER BY ar.testDate DESC")
    fun findRecentResultsByAthleteId(
        @Param("athleteId") athleteId: Long, 
        @Param("since") since: LocalDate
    ): List<AssessmentResult>
    
    // Get assessment progress (all results for athlete and assessment)
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.athlete.id = :athleteId AND ar.assessment.id = :assessmentId " +
           "AND ar.status = 'COMPLETED' ORDER BY ar.testDate ASC")
    fun findProgressByAthleteAndAssessment(
        @Param("athleteId") athleteId: Long, 
        @Param("assessmentId") assessmentId: Long
    ): List<AssessmentResult>
    
    // Count completed assessments for athlete
    @Query("SELECT COUNT(ar) FROM AssessmentResult ar WHERE ar.athlete.id = :athleteId AND ar.status = 'COMPLETED'")
    fun countCompletedAssessmentsByAthleteId(@Param("athleteId") athleteId: Long): Long
    
    // Find results by assessment category
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.athlete.id = :athleteId AND " +
           "ar.assessment.category = :category AND ar.status = 'COMPLETED' ORDER BY ar.testDate DESC")
    fun findByAthleteIdAndAssessmentCategory(
        @Param("athleteId") athleteId: Long, 
        @Param("category") category: String
    ): List<AssessmentResult>
    
    // Get athlete's best results (for assessments where higher is better)
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.athlete.id = :athleteId AND ar.assessment.id = :assessmentId " +
           "AND ar.status = 'COMPLETED' ORDER BY ar.value DESC LIMIT 1")
    fun findBestResultByAthleteAndAssessment(
        @Param("athleteId") athleteId: Long, 
        @Param("assessmentId") assessmentId: Long
    ): AssessmentResult?
    
    // Get athlete's best results (for assessments where lower is better - like sprint times)
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.athlete.id = :athleteId AND ar.assessment.id = :assessmentId " +
           "AND ar.status = 'COMPLETED' ORDER BY ar.value ASC LIMIT 1")
    fun findBestResultLowerBetterByAthleteAndAssessment(
        @Param("athleteId") athleteId: Long, 
        @Param("assessmentId") assessmentId: Long
    ): AssessmentResult?
    
    // Find existing result for duplicate prevention
    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.assessment.id = :assessmentId AND ar.athlete.id = :athleteId AND ar.testDate = :testDate")
    fun findByAssessmentIdAndAthleteIdAndTestDate(
        @Param("assessmentId") assessmentId: Long,
        @Param("athleteId") athleteId: Long,
        @Param("testDate") testDate: LocalDate
    ): AssessmentResult?
}