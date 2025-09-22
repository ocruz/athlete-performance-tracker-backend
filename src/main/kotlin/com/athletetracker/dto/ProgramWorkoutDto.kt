package com.athletetracker.dto

import java.time.LocalDateTime

data class ProgramWorkoutDto(
    val id: Long,
    val name: String,
    val description: String?,
    val workoutType: String,
    val estimatedDuration: Int?,
    val orderInProgram: Int,
    val notes: String?,
    val warmupInstructions: String?,
    val cooldownInstructions: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val exercises: List<ProgramWorkoutExerciseDto>
)

data class ProgramWorkoutBasicDto(
    val id: Long,
    val name: String,
    val description: String?,
    val workoutType: String,
    val estimatedDuration: Int?,
    val orderInProgram: Int,
    val exerciseCount: Int
)

data class ProgramWorkoutExerciseDto(
    val id: Long,
    val exercise: ExerciseBasicDto,
    val sets: Int?,
    val reps: Int?,
    val intensityPercentage: Double?,
    val weight: Double?,
    val distance: Double?,
    val time: Int?,
    val restTime: Int?,
    val orderInWorkout: Int,
    val notes: String?,
    val coachInstructions: String?,
    val progressionType: String?,
    val progressionValue: Double?,
    val isSuperset: Boolean,
    val supersetGroup: Int?,
    val isDropset: Boolean,
    val isFailure: Boolean
)

data class CreateProgramWorkoutRequest(
    val programId: Long,
    val name: String,
    val description: String?,
    val workoutType: String,
    val estimatedDuration: Int?,
    val orderInProgram: Int,
    val notes: String?,
    val warmupInstructions: String?,
    val cooldownInstructions: String?,
    val exercises: List<CreateProgramWorkoutExerciseRequest>
)

data class CreateProgramWorkoutExerciseRequest(
    val exerciseId: Long,
    val sets: Int?,
    val reps: Int?,
    val intensityPercentage: Double?,
    val weight: Double?,
    val distance: Double?,
    val time: Int?,
    val restTime: Int?,
    val orderInWorkout: Int,
    val notes: String?,
    val coachInstructions: String?,
    val progressionType: String?,
    val progressionValue: Double?,
    val isSuperset: Boolean = false,
    val supersetGroup: Int?,
    val isDropset: Boolean = false,
    val isFailure: Boolean = false
)

data class UpdateProgramWorkoutRequest(
    val name: String?,
    val description: String?,
    val workoutType: String?,
    val estimatedDuration: Int?,
    val orderInProgram: Int?,
    val notes: String?,
    val warmupInstructions: String?,
    val cooldownInstructions: String?
)

data class WorkoutTemplateLibraryDto(
    val id: Long,
    val name: String,
    val description: String?,
    val workoutType: String,
    val estimatedDuration: Int?,
    val exerciseCount: Int,
    val usageCount: Int, // How many programs use this template
    val isReusable: Boolean = true
)

data class GenerateWorkoutsRequest(
    val startDate: String, // ISO date string
    val numberOfWeeks: Int = 4
)

data class UpdateProgramWorkoutExerciseRequest(
    val sets: Int?,
    val reps: Int?,
    val intensityPercentage: Double?,
    val weight: Double?,
    val distance: Double?,
    val time: Int?,
    val restTime: Int?,
    val orderInWorkout: Int?,
    val notes: String?,
    val coachInstructions: String?,
    val progressionType: String?,
    val progressionValue: Double?,
    val isSuperset: Boolean?,
    val supersetGroup: Int?,
    val isDropset: Boolean?,
    val isFailure: Boolean?
)

data class ReorderExercisesRequest(
    val exerciseOrders: List<ExerciseOrderItem>
)

data class ExerciseOrderItem(
    val exerciseId: Long,
    val newOrder: Int
)

data class DuplicateWorkoutRequest(
    val name: String,
    val orderInProgram: Int?
)

data class ReorderWorkoutsRequest(
    val workoutOrders: List<WorkoutOrderItem>
)

data class WorkoutOrderItem(
    val workoutId: Long,
    val newOrder: Int
)