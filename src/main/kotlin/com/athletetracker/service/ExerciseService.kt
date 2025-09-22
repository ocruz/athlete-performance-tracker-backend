package com.athletetracker.service

import com.athletetracker.entity.Exercise
import com.athletetracker.entity.ExerciseCategory
import com.athletetracker.entity.MuscleGroup
import com.athletetracker.repository.ExerciseRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class ExerciseService(
    private val exerciseRepository: ExerciseRepository
) {

    fun createExercise(request: CreateExerciseRequest): Exercise {
        val exercise = Exercise(
            name = request.name,
            description = request.description,
            category = request.category,
            muscleGroup = request.muscleGroup,
            instructions = request.instructions,
            videoUrl = request.videoUrl,
            imageUrl = request.imageUrl
        )

        return exerciseRepository.save(exercise)
    }

    fun getExerciseById(id: Long): Exercise {
        return exerciseRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Exercise not found with id: $id") }
    }

    fun getAllActiveExercises(): List<Exercise> {
        return exerciseRepository.findByIsActiveTrue()
    }

    fun getExercisesByCategory(category: ExerciseCategory): List<Exercise> {
        return exerciseRepository.findByCategoryAndIsActiveTrue(category)
    }

    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): List<Exercise> {
        return exerciseRepository.findByMuscleGroupAndIsActiveTrue(muscleGroup)
    }

    fun searchExercises(query: String): List<Exercise> {
        return exerciseRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query)
    }

    fun updateExercise(id: Long, request: UpdateExerciseRequest): Exercise {
        val existingExercise = getExerciseById(id)

        val updatedExercise = existingExercise.copy(
            name = request.name ?: existingExercise.name,
            description = request.description ?: existingExercise.description,
            category = request.category ?: existingExercise.category,
            muscleGroup = request.muscleGroup ?: existingExercise.muscleGroup,
            instructions = request.instructions ?: existingExercise.instructions,
            videoUrl = request.videoUrl ?: existingExercise.videoUrl,
            imageUrl = request.imageUrl ?: existingExercise.imageUrl
        )

        return exerciseRepository.save(updatedExercise)
    }

    fun deleteExercise(id: Long) {
        val exercise = getExerciseById(id)
        val deactivatedExercise = exercise.copy(isActive = false)
        exerciseRepository.save(deactivatedExercise)
    }

    fun initializeDefaultExercises() {
        if (exerciseRepository.count() == 0L) {
            val defaultExercises = listOf(
                // Strength - Chest
                Exercise(name = "Barbell Bench Press", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.CHEST,
                    description = "Compound chest exercise using barbell"),
                Exercise(name = "Dumbbell Bench Press", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.CHEST,
                    description = "Chest exercise using dumbbells for greater range of motion"),
                Exercise(name = "Push-ups", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.CHEST,
                    description = "Bodyweight chest exercise"),
                
                // Strength - Back
                Exercise(name = "Deadlift", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.BACK,
                    description = "Compound exercise targeting posterior chain"),
                Exercise(name = "Pull-ups", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.BACK,
                    description = "Bodyweight back exercise"),
                Exercise(name = "Barbell Rows", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.BACK,
                    description = "Horizontal pulling exercise"),
                
                // Strength - Legs
                Exercise(name = "Barbell Squat", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.LEGS,
                    description = "Compound leg exercise"),
                Exercise(name = "Lunges", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.LEGS,
                    description = "Unilateral leg exercise"),
                Exercise(name = "Leg Press", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.LEGS,
                    description = "Machine-based leg exercise"),
                
                // Strength - Shoulders
                Exercise(name = "Overhead Press", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.SHOULDERS,
                    description = "Vertical pressing movement"),
                Exercise(name = "Lateral Raises", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.SHOULDERS,
                    description = "Isolation exercise for middle deltoids"),
                
                // Cardio
                Exercise(name = "Treadmill Running", category = ExerciseCategory.CARDIO, muscleGroup = MuscleGroup.CARDIO,
                    description = "Indoor running exercise"),
                Exercise(name = "Cycling", category = ExerciseCategory.CARDIO, muscleGroup = MuscleGroup.CARDIO,
                    description = "Low-impact cardio exercise"),
                Exercise(name = "Rowing Machine", category = ExerciseCategory.CARDIO, muscleGroup = MuscleGroup.FULL_BODY,
                    description = "Full-body cardio exercise"),
                
                // Plyometric
                Exercise(name = "Box Jumps", category = ExerciseCategory.PLYOMETRIC, muscleGroup = MuscleGroup.LEGS,
                    description = "Explosive leg exercise"),
                Exercise(name = "Burpees", category = ExerciseCategory.PLYOMETRIC, muscleGroup = MuscleGroup.FULL_BODY,
                    description = "Full-body explosive exercise"),
                
                // Core
                Exercise(name = "Planks", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.CORE,
                    description = "Isometric core exercise"),
                Exercise(name = "Crunches", category = ExerciseCategory.STRENGTH, muscleGroup = MuscleGroup.CORE,
                    description = "Traditional ab exercise")
            )

            exerciseRepository.saveAll(defaultExercises)
        }
    }
}

// Request DTOs
data class CreateExerciseRequest(
    val name: String,
    val description: String? = null,
    val category: ExerciseCategory,
    val muscleGroup: MuscleGroup,
    val instructions: String? = null,
    val videoUrl: String? = null,
    val imageUrl: String? = null
)

data class UpdateExerciseRequest(
    val name: String? = null,
    val description: String? = null,
    val category: ExerciseCategory? = null,
    val muscleGroup: MuscleGroup? = null,
    val instructions: String? = null,
    val videoUrl: String? = null,
    val imageUrl: String? = null
)