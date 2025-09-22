package com.athletetracker.repository

import com.athletetracker.entity.Workout
import com.athletetracker.entity.WorkoutExercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkoutExerciseRepository : JpaRepository<WorkoutExercise, Long> {
    fun findByWorkoutOrderByOrderInWorkout(workout: Workout): List<WorkoutExercise>
    fun deleteByWorkout(workout: Workout)
}