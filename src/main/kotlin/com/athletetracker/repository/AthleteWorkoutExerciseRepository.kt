package com.athletetracker.repository

import com.athletetracker.entity.AthleteWorkout
import com.athletetracker.entity.AthleteWorkoutExercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AthleteWorkoutExerciseRepository : JpaRepository<AthleteWorkoutExercise, Long> {
    fun deleteByAthleteWorkout(athleteWorkout: AthleteWorkout)
    fun findByAthleteWorkoutIdAndExerciseId(workoutId: Long, exerciseId: Long): AthleteWorkoutExercise?
    fun findByAthleteWorkoutOrderByOrderInWorkout(athleteWorkout: AthleteWorkout): List<AthleteWorkoutExercise>
    
    @Query("""
        SELECT COUNT(awe) FROM AthleteWorkoutExercise awe
        INNER JOIN awe.athleteWorkout aw
        INNER JOIN AthleteProgram ap ON ap.athlete = aw.athlete
        WHERE ap.id = :athleteProgramId 
        AND awe.completionStatus IN ('COMPLETED', 'MODIFIED')
    """)
    fun countCompletedExercisesByAthleteProgram(@Param("athleteProgramId") athleteProgramId: Long): Long
}