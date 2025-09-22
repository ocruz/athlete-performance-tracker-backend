package com.athletetracker.dto

import com.athletetracker.entity.MetricType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class CreatePerformanceMetricRequest(
    @field:NotNull(message = "Athlete ID is required")
    val athleteId: Long,
    
    @field:NotNull(message = "Metric type is required")
    val metricType: MetricType,
    
    @field:NotNull(message = "Value is required")
    @field:Positive(message = "Value must be positive")
    val metricValue: Double,
    
    val unit: String? = null,
    
    @field:NotNull(message = "Test date is required")
    val testDate: LocalDateTime,
    
    val notes: String? = null
)

data class PerformanceMetricResponse(
    val id: Long,
    val metricType: MetricType,
    val metricValue: Double,
    val unit: String?,
    val testDate: LocalDateTime,
    val notes: String?,
    val recordedBy: String?
)

data class MetricProgressResponse(
    val metricType: MetricType,
    val currentValue: Double?,
    val previousValue: Double?,
    val improvement: Double?,
    val improvementPercentage: Double?,
    val history: List<PerformanceMetricResponse>
)