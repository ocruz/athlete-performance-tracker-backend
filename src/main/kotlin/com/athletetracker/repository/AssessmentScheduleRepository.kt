package com.athletetracker.repository

import com.athletetracker.entity.AssessmentSchedule
import com.athletetracker.entity.ScheduleStatus
import com.athletetracker.entity.RecurrenceType
import com.athletetracker.entity.Athlete
import com.athletetracker.entity.Assessment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface AssessmentScheduleRepository : JpaRepository<AssessmentSchedule, Long> {
    
    // Find scheduled assessments for athlete
    @Query("SELECT asch FROM AssessmentSchedule asch JOIN FETCH asch.assessment JOIN FETCH asch.athlete WHERE asch.athlete.id = :athleteId AND asch.isActive = true ORDER BY asch.scheduledDate ASC")
    fun findByAthleteIdAndIsActiveTrue(@Param("athleteId") athleteId: Long): List<AssessmentSchedule>
    
    // Find scheduled assessments by status
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.status = :status AND asch.isActive = true ORDER BY asch.scheduledDate ASC")
    fun findByStatusAndIsActiveTrue(@Param("status") status: ScheduleStatus): List<AssessmentSchedule>
    
    // Find upcoming assessments (scheduled for today or future)
    @Query("SELECT asch FROM AssessmentSchedule asch JOIN FETCH asch.assessment JOIN FETCH asch.athlete WHERE asch.scheduledDate >= :date AND " +
           "asch.status IN ('SCHEDULED', 'CONFIRMED') AND asch.isActive = true ORDER BY asch.scheduledDate ASC")
    fun findUpcomingAssessments(@Param("date") date: LocalDate): List<AssessmentSchedule>
    
    // Find assessments scheduled for specific date
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.scheduledDate = :date AND asch.isActive = true ORDER BY asch.scheduledTime ASC")
    fun findByScheduledDateAndIsActiveTrue(@Param("date") date: LocalDate): List<AssessmentSchedule>
    
    // Find assessments in date range
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.scheduledDate BETWEEN :startDate AND :endDate " +
           "AND asch.isActive = true ORDER BY asch.scheduledDate ASC")
    fun findByDateRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<AssessmentSchedule>
    
    // Find assessments scheduled by specific user
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.scheduledBy.id = :userId AND asch.isActive = true ORDER BY asch.scheduledDate ASC")
    fun findByScheduledByIdAndIsActiveTrue(@Param("userId") userId: Long): List<AssessmentSchedule>
    
    // Find recurring assessments
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.recurrenceType IS NOT NULL AND asch.isActive = true ORDER BY asch.scheduledDate ASC")
    fun findRecurringAssessments(): List<AssessmentSchedule>
    
    // Find assessments that need reminders
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.scheduledDate = :reminderDate AND " +
           "asch.reminderSent = false AND asch.status IN ('SCHEDULED', 'CONFIRMED') AND asch.isActive = true")
    fun findAssessmentsNeedingReminder(@Param("reminderDate") reminderDate: LocalDate): List<AssessmentSchedule>
    
    // Find overdue assessments (scheduled in the past but not completed)
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.scheduledDate < :currentDate AND " +
           "asch.status IN ('SCHEDULED', 'CONFIRMED') AND asch.isActive = true ORDER BY asch.scheduledDate ASC")
    fun findOverdueAssessments(@Param("currentDate") currentDate: LocalDate): List<AssessmentSchedule>
    
    // Find athlete's upcoming assessments (next 30 days)
    @Query("SELECT asch FROM AssessmentSchedule asch JOIN FETCH asch.assessment JOIN FETCH asch.athlete WHERE asch.athlete.id = :athleteId AND " +
           "asch.scheduledDate BETWEEN :currentDate AND :endDate AND " +
           "asch.status IN ('SCHEDULED', 'CONFIRMED') AND asch.isActive = true ORDER BY asch.scheduledDate ASC")
    fun findUpcomingAssessmentsForAthlete(
        @Param("athleteId") athleteId: Long,
        @Param("currentDate") currentDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<AssessmentSchedule>
    
    // Find assessments by athlete and assessment type
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.athlete.id = :athleteId AND " +
           "asch.assessment.id = :assessmentId AND asch.isActive = true ORDER BY asch.scheduledDate DESC")
    fun findByAthleteIdAndAssessmentId(
        @Param("athleteId") athleteId: Long,
        @Param("assessmentId") assessmentId: Long
    ): List<AssessmentSchedule>
    
    // Count scheduled assessments for athlete
    @Query("SELECT COUNT(asch) FROM AssessmentSchedule asch WHERE asch.athlete.id = :athleteId AND asch.isActive = true")
    fun countByAthleteIdAndIsActiveTrue(@Param("athleteId") athleteId: Long): Long
    
    // Find assessments by recurrence type
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.recurrenceType = :recurrenceType AND asch.isActive = true")
    fun findByRecurrenceType(@Param("recurrenceType") recurrenceType: RecurrenceType): List<AssessmentSchedule>
    
    // Find schedules by athlete, assessment and date for completion tracking
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.athlete = :athlete AND asch.assessment = :assessment AND asch.scheduledDate = :date")
    fun findByAthleteAndAssessmentAndDate(
        @Param("athlete") athlete: Athlete,
        @Param("assessment") assessment: Assessment,
        @Param("date") date: LocalDate
    ): List<AssessmentSchedule>
    
    // Find all schedules by assessment
    @Query("SELECT asch FROM AssessmentSchedule asch WHERE asch.assessment.id = :assessmentId")
    fun findByAssessmentId(@Param("assessmentId") assessmentId: Long): List<AssessmentSchedule>
}