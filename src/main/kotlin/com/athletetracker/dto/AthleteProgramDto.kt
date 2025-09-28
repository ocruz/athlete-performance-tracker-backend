package com.athletetracker.dto

import com.athletetracker.entity.ProgramStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class AssignProgramRequest(
    val athleteId: Long,
    val programId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val notes: String? = null
)

data class AthleteProgramResponse(
    val id: Long,
    val athlete: AthleteBasicDto,
    val program: ProgramBasicDto,
    val assignedBy: UserBasicDto,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val status: ProgramStatus,
    val notes: String?,
    val createdAt: LocalDateTime,
    val progressSummary: ProgramProgressSummary
)

data class ProgramProgressSummary(
    val totalExercises: Int,
    val completedExercises: Int,
    val completionPercentage: Double,
    val currentWeek: Int?,
    val lastActivityDate: LocalDate?,
    val adherenceRate: Double // percentage of scheduled workouts completed
)

data class UpdateProgramStatusRequest(
    val status: ProgramStatus,
    val notes: String? = null
)

data class AthleteBasicDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val sport: String,
    val fullName: String? = null,
    val position: String? = null,
    val email: String? = null
)

data class ProgramBasicDto(
    val id: Long,
    val name: String,
    val description: String?,
    val sport: String,
    val durationWeeks: Int?,
    val difficultyLevel: String?
)

data class UserBasicDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String
)

data class ProgramDetailResponse(
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
    val createdAt: String,
    val updatedAt: String,
    val workouts: List<ProgramWorkoutDto>,
    val summary: ProgramSummary
)

data class ProgramSummary(
    val totalWorkouts: Int,
    val totalExercises: Int,
    val estimatedWeeklyDuration: Int?, // Total minutes per week
    val workoutTypeBreakdown: Map<String, Int> // WorkoutType -> count
)