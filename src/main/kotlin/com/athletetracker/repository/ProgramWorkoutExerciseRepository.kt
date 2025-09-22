package com.athletetracker.repository

import com.athletetracker.entity.Exercise
import com.athletetracker.entity.ProgramWorkout
import com.athletetracker.entity.ProgramWorkoutExercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProgramWorkoutExerciseRepository : JpaRepository<ProgramWorkoutExercise, Long> {
    
    // Find all exercises for a workout template, ordered by their sequence
    fun findByProgramWorkoutOrderByOrderInWorkoutAsc(programWorkout: ProgramWorkout): List<ProgramWorkoutExercise>
    
    // Find exercises by workout template ID
    fun findByProgramWorkoutIdOrderByOrderInWorkoutAsc(programWorkoutId: Long): List<ProgramWorkoutExercise>
    
    // Find exercises in a superset group
    fun findByProgramWorkoutAndIsSupersetTrueAndSupersetGroupOrderByOrderInWorkoutAsc(
        programWorkout: ProgramWorkout,
        supersetGroup: Int
    ): List<ProgramWorkoutExercise>
    
    // Find all superset groups in a workout
    @Query("SELECT DISTINCT pwe.supersetGroup FROM ProgramWorkoutExercise pwe WHERE pwe.programWorkout = :programWorkout AND pwe.isSuperset = true ORDER BY pwe.supersetGroup")
    fun findSupersetGroupsByProgramWorkout(@Param("programWorkout") programWorkout: ProgramWorkout): List<Int>
    
    // Find exercises by specific exercise type
    fun findByProgramWorkoutAndExercise(programWorkout: ProgramWorkout, exercise: Exercise): List<ProgramWorkoutExercise>
    
    // Get exercise with full details
    @Query("SELECT pwe FROM ProgramWorkoutExercise pwe JOIN FETCH pwe.exercise WHERE pwe.id = :exerciseId")
    fun findByIdWithExercise(@Param("exerciseId") exerciseId: Long): ProgramWorkoutExercise?
    
    // Count exercises in a workout template
    fun countByProgramWorkout(programWorkout: ProgramWorkout): Long
    
    // Find exercises that use progression
    @Query("SELECT pwe FROM ProgramWorkoutExercise pwe WHERE pwe.programWorkout = :programWorkout AND pwe.progressionType IS NOT NULL ORDER BY pwe.orderInWorkout")
    fun findByProgramWorkoutWithProgression(@Param("programWorkout") programWorkout: ProgramWorkout): List<ProgramWorkoutExercise>
    
    // Find exercises by intensity range
    @Query("SELECT pwe FROM ProgramWorkoutExercise pwe WHERE pwe.programWorkout = :programWorkout AND pwe.intensityPercentage BETWEEN :minIntensity AND :maxIntensity ORDER BY pwe.orderInWorkout")
    fun findByProgramWorkoutAndIntensityRange(
        @Param("programWorkout") programWorkout: ProgramWorkout,
        @Param("minIntensity") minIntensity: Double,
        @Param("maxIntensity") maxIntensity: Double
    ): List<ProgramWorkoutExercise>
    
    // Delete all exercises for a workout template
    fun deleteByProgramWorkout(programWorkout: ProgramWorkout)
    
    // Update order of exercises in batch
    @Query("UPDATE ProgramWorkoutExercise pwe SET pwe.orderInWorkout = :newOrder WHERE pwe.id = :exerciseId")
    fun updateOrderInWorkout(@Param("exerciseId") exerciseId: Long, @Param("newOrder") newOrder: Int)
}