package com.athletetracker.dto

import java.time.LocalDate

data class AthleteDashboardResponse(
    val athlete: AthleteBasicDto,
    val activePrograms: List<AthleteProgramSummaryDto>,
    val progressHighlights: ProgressHighlightsDto,
    val upcomingWorkouts: List<AthleteWorkoutSummaryDto>
)

// Using AthleteBasicDto from AthleteProgramDto.kt to avoid duplication
// Using ProgramSummaryDto from EnhancedProgramDto.kt to avoid duplication  
// Using WorkoutSummaryDto from AthleteWorkoutDto.kt to avoid duplication

data class AthleteProgramSummaryDto(
    val id: Long,
    val name: String,
    val completionPercentage: Double,
    val currentWeek: Int?,
    val adherenceRate: Double,
    val startDate: LocalDate,
    val endDate: LocalDate?
)

data class ProgressHighlightsDto(
    val thisWeekWorkouts: Int,
    val totalWorkouts: Int,
    val averageRPE: Double,
    val strengthImprovement: Double
)