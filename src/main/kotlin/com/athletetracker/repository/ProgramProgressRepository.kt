package com.athletetracker.repository

import com.athletetracker.entity.CompletionStatus
import com.athletetracker.entity.AthleteExerciseCompletions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ProgramProgressRepository : JpaRepository<AthleteExerciseCompletions, Long> {
    
    fun findByAthleteProgramId(athleteProgramId: Long): List<AthleteExerciseCompletions>
    
    fun findByAthleteProgramIdAndCompletedDateBetween(
        athleteProgramId: Long, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<AthleteExerciseCompletions>
    
    @Query(
        """
        SELECT COUNT(pp) FROM AthleteExerciseCompletions pp 
        WHERE pp.athleteProgram.id = :athleteProgramId 
        AND pp.completionStatus = :status
    """
    )
    fun countByAthleteProgramIdAndStatus(
        @Param("athleteProgramId") athleteProgramId: Long,
        @Param("status") status: CompletionStatus
    ): Long
    
    @Query(
        """
        SELECT COUNT(DISTINCT pp.programWorkoutExercise.id) FROM AthleteExerciseCompletions pp 
        WHERE pp.athleteProgram.id = :athleteProgramId
    """
    )
    fun countCompletedExercisesByAthleteProgramId(@Param("athleteProgramId") athleteProgramId: Long): Long
    
    @Query(
        """
        SELECT pp FROM AthleteExerciseCompletions pp 
        WHERE pp.athleteProgram.id = :athleteProgramId 
        AND pp.programWorkoutExercise.id = :programWorkoutExerciseId
    """
    )
    fun findByAthleteProgramIdAndProgramWorkoutExerciseId(
        @Param("athleteProgramId") athleteProgramId: Long,
        @Param("programWorkoutExerciseId") programWorkoutExerciseId: Long
    ): AthleteExerciseCompletions?
    
    @Query(
        """
        SELECT pp FROM AthleteExerciseCompletions pp 
        JOIN pp.athleteProgram ap 
        WHERE ap.athlete.id = :athleteId 
        AND FUNCTION('WEEK', pp.completedDate) = :week
    """
    )
    fun findByAthleteIdAndWeek(@Param("athleteId") athleteId: Long, @Param("week") week: Int): List<AthleteExerciseCompletions>
    
    @Query(
        """
        SELECT pp FROM AthleteExerciseCompletions pp 
        JOIN pp.athleteProgram ap 
        WHERE ap.athlete.id = :athleteId
    """
    )
    fun findByAthleteId(@Param("athleteId") athleteId: Long): List<AthleteExerciseCompletions>
}