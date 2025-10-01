package com.athletetracker.dto

import java.time.LocalDateTime

data class AthleteWorkoutDto(
    val id: Long,
    val athlete: AthleteBasicDto,
    val coach: UserBasicDto,
    val programWorkout: ProgramWorkoutBasicDto?, // Link to workout template
    val workoutDate: LocalDateTime,
    val name: String?,
    val notes: String?,
    val rpe: Int?,
    val duration: Int?,
    val createdAt: LocalDateTime,
    val workoutExercises: List<AthleteWorkoutExerciseDto>,
    val summary: AthleteWorkoutSummaryDto
)

data class AthleteWorkoutExerciseDto(
    val id: Long,
    val exercise: ExerciseBasicDto,
    val programWorkoutExercise: ProgramWorkoutExerciseDto?, // Link to template exercise
    val planned: PlannedExerciseDto?,
    val actual: ActualExerciseDto?,
    val notes: String?,
    val orderInWorkout: Int,
    val completionStatus: String,
    val exerciseRpe: Int?,
    val isFromProgram: Boolean,
    // Legacy fields for backward compatibility
    val sets: Int?,
    val reps: Int?,
    val weight: Double?,
    val distance: Double?,
    val time: Int?,
    val restTime: Int?
)

data class PlannedExerciseDto(
    val sets: Int?,
    val reps: Int?,
    val weight: Double?,
    val distance: Double?,
    val time: Int?,
    val restTime: Int?,
    val intensity: Double? // % of 1RM
)

data class ActualExerciseDto(
    val sets: Int?,
    val reps: Int?,
    val weight: Double?,
    val distance: Double?,
    val time: Int?,
    val restTime: Int?,
    val intensity: Double?
)

data class AthleteWorkoutSummaryDto(
    val totalExercises: Int,
    val completedExercises: Int,
    val skippedExercises: Int,
    val modifiedExercises: Int,
    val averageRpe: Double?,
    val adherencePercentage: Double, // How well athlete followed the plan
    val workoutType: String?,
    val estimatedDuration: Int?,
    val actualDuration: Int?
)

// ExerciseBasicDto is defined in ProgramProgressDto.kt to avoid duplication