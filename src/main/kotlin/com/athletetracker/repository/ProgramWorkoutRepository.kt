package com.athletetracker.repository

import com.athletetracker.entity.Program
import com.athletetracker.entity.ProgramWorkout
import com.athletetracker.entity.WorkoutType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProgramWorkoutRepository : JpaRepository<ProgramWorkout, Long> {
    
    // Find all workout templates for a program, ordered by their sequence
    fun findByProgramOrderByOrderInProgramAsc(program: Program): List<ProgramWorkout>
    
    // Find workout templates by program ID
    fun findByProgramIdOrderByOrderInProgramAsc(programId: Long): List<ProgramWorkout>
    
    // Find workout templates by type within a program
    fun findByProgramAndWorkoutTypeOrderByOrderInProgramAsc(
        program: Program, 
        workoutType: WorkoutType
    ): List<ProgramWorkout>
    
    // Find active workout templates for a program
    fun findByProgramAndIsActiveTrueOrderByOrderInProgramAsc(program: Program): List<ProgramWorkout>
    
    // Get workout template with exercises loaded
    @Query("SELECT pw FROM ProgramWorkout pw JOIN FETCH pw.exercises WHERE pw.id = :workoutId")
    fun findByIdWithExercises(@Param("workoutId") workoutId: Long): ProgramWorkout?
    
    // Get all workout templates for a program with exercises loaded
    @Query("SELECT DISTINCT pw FROM ProgramWorkout pw LEFT JOIN FETCH pw.exercises WHERE pw.program = :program ORDER BY pw.orderInProgram")
    fun findByProgramWithExercises(@Param("program") program: Program): List<ProgramWorkout>
    
    // Count workout templates by type in a program
    fun countByProgramAndWorkoutType(program: Program, workoutType: WorkoutType): Long
    
    // Count all workout templates in a program
    fun countByProgram(program: Program): Long
    
    // Count active workout templates
    fun countByIsActive(isActive: Boolean): Long
    
    // Find workout templates by estimated duration range
    @Query("SELECT pw FROM ProgramWorkout pw WHERE pw.program = :program AND pw.estimatedDuration BETWEEN :minDuration AND :maxDuration ORDER BY pw.orderInProgram")
    fun findByProgramAndDurationRange(
        @Param("program") program: Program,
        @Param("minDuration") minDuration: Int,
        @Param("maxDuration") maxDuration: Int
    ): List<ProgramWorkout>
    
    // Delete all workout templates for a program
    fun deleteByProgram(program: Program)
}