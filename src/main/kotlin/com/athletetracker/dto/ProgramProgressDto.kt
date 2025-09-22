package com.athletetracker.dto

import com.athletetracker.entity.CompletionStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class LogProgressRequest(
    val athleteProgramId: Long,
    val programWorkoutExerciseId: Long,
    val completedDate: LocalDate,
    val actualSets: Int? = null,
    val actualReps: Int? = null,
    val actualWeight: Double? = null,
    val actualIntensity: Double? = null,
    val restTime: Int? = null,
    val completionStatus: CompletionStatus = CompletionStatus.COMPLETED,
    val athleteNotes: String? = null,
    val coachNotes: String? = null
)

data class ProgramProgressResponse(
    val id: Long,
    val athleteProgram: AthleteProgramBasicDto,
    val exercise: ExerciseBasicDto,
    val weekNumber: Int,
    val dayNumber: Int,
    val orderInDay: Int,
    val completedDate: LocalDate,
    val plannedSets: Int?,
    val plannedReps: Int?,
    val plannedIntensity: Double?,
    val actualSets: Int?,
    val actualReps: Int?,
    val actualWeight: Double?,
    val actualIntensity: Double?,
    val restTime: Int?,
    val completionStatus: CompletionStatus,
    val athleteNotes: String?,
    val coachNotes: String?,
    val createdAt: LocalDateTime,
    val loggedBy: UserBasicDto
)

data class WeekProgressResponse(
    val weekNumber: Int,
    val totalExercises: Int,
    val completedExercises: Int,
    val completionPercentage: Double,
    val exercises: List<DayExerciseProgress>
)

data class DayExerciseProgress(
    val dayNumber: Int,
    val exercises: List<ExerciseProgressSummary>
)

data class ExerciseProgressSummary(
    val programWorkoutExerciseId: Long,
    val exercise: ExerciseBasicDto,
    val orderInDay: Int,
    val plannedSets: Int?,
    val plannedReps: Int?,
    val plannedIntensity: Double?,
    val isCompleted: Boolean,
    val completionStatus: CompletionStatus?,
    val lastCompletedDate: LocalDate?
)

data class AthleteProgramBasicDto(
    val id: Long,
    val athleteName: String,
    val programName: String,
    val startDate: LocalDate,
    val status: String
)

data class ExerciseBasicDto(
    val id: Long,
    val name: String,
    val category: String,
    val muscleGroups: String
)