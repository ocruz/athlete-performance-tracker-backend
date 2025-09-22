package com.athletetracker.repository

import com.athletetracker.entity.CompletionStatus
import com.athletetracker.entity.ProgramProgress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ProgramProgressRepository : JpaRepository<ProgramProgress, Long> {
    
    fun findByAthleteProgramId(athleteProgramId: Long): List<ProgramProgress>
    
    fun findByAthleteProgramIdAndCompletedDateBetween(
        athleteProgramId: Long, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<ProgramProgress>
    
    @Query("""
        SELECT COUNT(pp) FROM ProgramProgress pp 
        WHERE pp.athleteProgram.id = :athleteProgramId 
        AND pp.completionStatus = :status
    """)
    fun countByAthleteProgramIdAndStatus(
        @Param("athleteProgramId") athleteProgramId: Long,
        @Param("status") status: CompletionStatus
    ): Long
    
    @Query("""
        SELECT COUNT(DISTINCT pp.programWorkoutExercise.id) FROM ProgramProgress pp 
        WHERE pp.athleteProgram.id = :athleteProgramId
    """)
    fun countCompletedExercisesByAthleteProgramId(@Param("athleteProgramId") athleteProgramId: Long): Long
    
    @Query("""
        SELECT pp FROM ProgramProgress pp 
        WHERE pp.athleteProgram.id = :athleteProgramId 
        AND pp.programWorkoutExercise.id = :programWorkoutExerciseId
    """)
    fun findByAthleteProgramIdAndProgramWorkoutExerciseId(
        @Param("athleteProgramId") athleteProgramId: Long,
        @Param("programWorkoutExerciseId") programWorkoutExerciseId: Long
    ): ProgramProgress?
    
    @Query("""
        SELECT pp FROM ProgramProgress pp 
        JOIN pp.athleteProgram ap 
        WHERE ap.athlete.id = :athleteId 
        AND FUNCTION('WEEK', pp.completedDate) = :week
    """)
    fun findByAthleteIdAndWeek(@Param("athleteId") athleteId: Long, @Param("week") week: Int): List<ProgramProgress>
    
    @Query("""
        SELECT pp FROM ProgramProgress pp 
        JOIN pp.athleteProgram ap 
        WHERE ap.athlete.id = :athleteId
    """)
    fun findByAthleteId(@Param("athleteId") athleteId: Long): List<ProgramProgress>
}