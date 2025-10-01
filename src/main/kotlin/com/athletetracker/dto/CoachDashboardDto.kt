package com.athletetracker.dto

import java.time.LocalDate

/**
 * DTOs for Coach Dashboard and Analytics functionality
 */

data class CoachDashboardResponse(
    val coach: CoachBasicInfo,
    val managedAthletes: List<AthleteOverview>,
    val recentAssignments: List<RecentAssignment>,
    val programStats: ProgramStats
)

data class CoachBasicInfo(
    val id: Long,
    val name: String,
    val email: String
)

data class AthleteOverview(
    val id: Long,
    val name: String,
    val sport: String,
    val activePrograms: Int,
    val lastActivity: LocalDate?
)

data class RecentAssignment(
    val id: Long,
    val athleteName: String,
    val programName: String,
    val assignedDate: LocalDate,
    val status: String
)

data class ProgramStats(
    val totalPrograms: Int,
    val activeAssignments: Int,
    val completionRate: Double
)

data class AthleteWithPrograms(
    val athlete: AthleteBasicDto,
    val assignedPrograms: List<AthleteProgramResponse>,
    val progressSummary: AthleteProgressSummary
)

data class AthleteProgressSummary(
    val totalPrograms: Int,
    val activePrograms: Int,
    val completedPrograms: Int,
    val averageCompletion: Double
)

data class TeamAnalyticsResponse(
    val totalAthletes: Int,
    val totalPrograms: Int,
    val averageCompletion: Double,
    val topPerformers: List<AthletePerformanceSummary>,
    val programEffectiveness: List<ProgramEffectivenessSummary>
)

data class AthletePerformanceSummary(
    val athleteId: Long,
    val athleteName: String,
    val completionRate: Double,
    val averageRPE: Double,
    val strengthGains: Double
)

data class ProgramEffectivenessSummary(
    val programId: Long,
    val programName: String,
    val averageCompletion: Double,
    val athleteCount: Int,
    val effectivenessScore: Double
)