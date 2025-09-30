package com.athletetracker.dto

import com.athletetracker.entity.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Basic Assessment DTO for API responses
data class AssessmentDto(
    val id: Long,
    val name: String,
    val description: String?,
    val category: AssessmentCategory,
    val type: AssessmentType,
    val instructions: String?,
    val unit: String?,
    val scoringType: String?,
    val targetValue: Double?,
    val minValue: Double?,
    val maxValue: Double?,
    val equipmentRequired: String?,
    val estimatedDuration: Int?,
    val sport: Sport,
    val createdBy: UserBasicDto,
    val isActive: Boolean,
    val isTemplate: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// Assessment creation request
data class CreateAssessmentRequest(
    val name: String,
    val description: String?,
    val category: AssessmentCategory,
    val type: AssessmentType,
    val instructions: String?,
    val unit: String?,
    val scoringType: String?,
    val targetValue: Double?,
    val minValue: Double?,
    val maxValue: Double?,
    val equipmentRequired: String?,
    val estimatedDuration: Int?,
    val sport: Sport,
    val createdById: Long,
    val isTemplate: Boolean = true
)

// Assessment update request
data class UpdateAssessmentRequest(
    val name: String?,
    val description: String?,
    val instructions: String?,
    val unit: String?,
    val scoringType: String?,
    val targetValue: Double?,
    val minValue: Double?,
    val maxValue: Double?,
    val equipmentRequired: String?,
    val estimatedDuration: Int?,
    val isActive: Boolean?
)

// Assessment search request
data class AssessmentSearchRequest(
    val query: String?,
    val category: AssessmentCategory?,
    val sport: Sport?,
    val type: AssessmentType?,
    val isTemplate: Boolean?,
    val equipmentRequired: String?,
    val minDuration: Int?,
    val maxDuration: Int?
)

// Assessment with statistics
data class AssessmentWithStatsDto(
    val assessment: AssessmentDto,
    val totalResults: Long,
    val averageScore: Double?,
    val bestScore: Double?,
    val lastUsed: LocalDate?
)

// Assessment Result DTOs
data class AssessmentResultDto(
    val id: Long,
    val assessment: AssessmentBasicDto,
    val athlete: AthleteBasicDto,
    val testDate: LocalDate,
    val value: Double,
    val rawValue: String?,
    val status: AssessmentStatus,
    val notes: String?,
    val conditions: String?,
    val percentileRank: Double?,
    val improvementFromBaseline: Double?,
    val improvementPercentage: Double?,
    val isBaseline: Boolean,
    val videoUrl: String?,
    val conductedBy: UserBasicDto?,
    val createdAt: LocalDateTime
)

// Create assessment result request
data class CreateAssessmentResultRequest(
    val assessmentId: Long,
    val athleteId: Long,
    val testDate: LocalDate,
    val value: Double,
    val rawValue: String?,
    val notes: String?,
    val conditions: String?,
    val isBaseline: Boolean = false,
    val videoUrl: String?,
    val conductedById: Long?,
    val assessmentScheduleId: Long? = null
)

// Update assessment result request
data class UpdateAssessmentResultRequest(
    val value: Double?,
    val rawValue: String?,
    val status: AssessmentStatus?,
    val notes: String?,
    val conditions: String?,
    val isBaseline: Boolean?,
    val videoUrl: String?
)

// Assessment progress response
data class AssessmentProgressResponse(
    val assessment: AssessmentBasicDto,
    val athlete: AthleteBasicDto,
    val results: List<AssessmentResultDto>,
    val baseline: AssessmentResultDto?,
    val latest: AssessmentResultDto?,
    val best: AssessmentResultDto?,
    val totalImprovement: Double?,
    val totalImprovementPercentage: Double?,
    val trend: String, // "improving", "declining", "stable"
    val chartData: List<AssessmentChartDataPoint>
)

// Chart data point for assessment progress
data class AssessmentChartDataPoint(
    val date: LocalDate,
    val value: Double,
    val label: String?,
    val isBaseline: Boolean = false
)

// Assessment Schedule DTOs
data class AssessmentScheduleDto(
    val id: Long,
    val assessment: AssessmentBasicDto,
    val athlete: AthleteBasicDto,
    val scheduledDate: LocalDate,
    val scheduledTime: LocalTime?,
    val status: ScheduleStatus,
    val recurrenceType: RecurrenceType?,
    val recurrenceInterval: Int?,
    val recurrenceEndDate: LocalDate?,
    val maxRecurrences: Int?,
    val notes: String?,
    val specialInstructions: String?,
    val location: String?,
    val reminderSent: Boolean,
    val reminderDate: LocalDateTime?,
    val scheduledBy: UserBasicDto,
    val createdAt: LocalDateTime
)

// Create assessment schedule request
data class CreateAssessmentScheduleRequest(
    val assessmentId: Long,
    val athleteId: Long,
    val scheduledDate: LocalDate,
    val scheduledTime: LocalTime?,
    val recurrenceType: RecurrenceType?,
    val recurrenceInterval: Int?,
    val recurrenceEndDate: LocalDate?,
    val maxRecurrences: Int?,
    val notes: String?,
    val specialInstructions: String?,
    val location: String?,
    val scheduledById: Long
)

// Update schedule request
data class UpdateAssessmentScheduleRequest(
    val scheduledDate: LocalDate?,
    val scheduledTime: LocalTime?,
    val status: ScheduleStatus?,
    val notes: String?,
    val specialInstructions: String?,
    val location: String?
)

// Basic DTOs for references
data class AssessmentBasicDto(
    val id: Long,
    val name: String,
    val category: AssessmentCategory,
    val type: AssessmentType,
    val unit: String?,
    val sport: Sport
)

// Assessment summary for athlete
data class AthleteAssessmentSummaryDto(
    val athlete: AthleteBasicDto,
    val totalAssessments: Long,
    val completedAssessments: Long,
    val upcomingAssessments: Long,
    val lastAssessmentDate: LocalDate?,
    val recentResults: List<AssessmentResultDto>,
    val upcomingSchedules: List<AssessmentScheduleDto>
)

// Assessment analytics response
data class AssessmentAnalyticsResponse(
    val totalAssessments: Long,
    val totalResults: Long,
    val totalSchedules: Long,
    val assessmentsByCategory: Map<AssessmentCategory, Long>,
    val resultsByStatus: Map<AssessmentStatus, Long>,
    val schedulesByStatus: Map<ScheduleStatus, Long>,
    val popularAssessments: List<AssessmentWithStatsDto>,
    val recentActivity: List<AssessmentActivityDto>
)

// Assessment activity for dashboard
data class AssessmentActivityDto(
    val id: Long,
    val type: String, // "result_recorded", "assessment_scheduled", "assessment_completed"
    val assessment: AssessmentBasicDto,
    val athlete: AthleteBasicDto,
    val date: LocalDateTime,
    val description: String
)

// Assessment statistics response
data class AssessmentStatisticsResponse(
    val assessment: AssessmentBasicDto,
    val totalTimesUsed: Long,
    val totalResults: Long,
    val lastUsed: LocalDate?,
    val averageScore: Double?,
    val bestScore: Double?,
    val worstScore: Double?,
    val recentUsage: List<AssessmentUsageDto>
)

// Assessment usage data point
data class AssessmentUsageDto(
    val date: LocalDate,
    val athleteCount: Long,
    val averageScore: Double?
)