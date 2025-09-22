package com.athletetracker.dto

import java.time.LocalDateTime

// Enhanced Program DTO with workout templates
data class ProgramDetailDto(
    val id: Long,
    val name: String,
    val description: String?,
    val sport: String,
    val durationWeeks: Int?,
    val difficultyLevel: String?,
    val goals: String?,
    val createdBy: UserBasicDto,
    val isActive: Boolean,
    val isTemplate: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val programWorkouts: List<ProgramWorkoutDto>,
    val summary: ProgramSummaryDto
)

data class ProgramSummaryDto(
    val totalWorkouts: Int,
    val workoutTypeBreakdown: Map<String, Int>, // WorkoutType -> count
    val totalExercises: Int,
    val estimatedWeeklyDuration: Int?, // Total minutes per week
    val workoutCategories: List<String>,
    val difficultyScore: Double? // Calculated based on intensity, volume, etc.
)

data class CreateProgramWithWorkoutsRequest(
    val name: String,
    val description: String?,
    val sport: String,
    val durationWeeks: Int?,
    val difficultyLevel: String?,
    val goals: String?,
    val isTemplate: Boolean = false,
    val programWorkouts: List<CreateProgramWorkoutRequest>
)

data class ProgramBuilderDto(
    val program: ProgramDetailDto,
    val availableExercises: List<ExerciseBasicDto>,
    val workoutTemplateLibrary: List<WorkoutTemplateLibraryDto>,
    val recommendedProgression: ProgressionRecommendationDto?
)

data class ProgressionRecommendationDto(
    val recommendedChanges: List<String>,
    val weeklyProgressionSuggestions: Map<Int, String>, // Week -> suggestion
    val intensityProgression: List<Double>, // Week-by-week intensity recommendations
    val volumeProgression: List<Int> // Week-by-week volume recommendations
)

// Workout scheduling DTOs
data class WorkoutScheduleDto(
    val programWorkout: ProgramWorkoutBasicDto,
    val suggestedDays: List<String>, // ["Monday", "Wednesday", "Friday"]
    val requiredRestDays: Int,
    val canBeScheduledWith: List<Long>, // Other workout IDs that can be on same day
    val shouldNotFollow: List<Long> // Workout IDs that shouldn't come after this one
)

data class ProgramSchedulingDto(
    val program: ProgramBasicDto,
    val weeklySchedule: Map<Int, List<WorkoutScheduleDto>>, // Week -> Workouts
    val restDayRecommendations: List<String>,
    val totalWeeklyDuration: Int
)