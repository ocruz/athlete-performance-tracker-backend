package com.athletetracker.repository

import com.athletetracker.entity.Exercise
import com.athletetracker.entity.ExerciseCategory
import com.athletetracker.entity.MuscleGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseRepository : JpaRepository<Exercise, Long> {
    fun findByCategoryAndIsActiveTrue(category: ExerciseCategory): List<Exercise>
    fun findByMuscleGroupAndIsActiveTrue(muscleGroup: MuscleGroup): List<Exercise>
    fun findByIsActiveTrue(): List<Exercise>
    fun findByNameContainingIgnoreCaseAndIsActiveTrue(name: String): List<Exercise>
}