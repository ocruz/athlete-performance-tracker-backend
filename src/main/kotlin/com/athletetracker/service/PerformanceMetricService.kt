package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.MetricType
import com.athletetracker.entity.PerformanceMetric
import com.athletetracker.repository.AthleteRepository
import com.athletetracker.repository.PerformanceMetricRepository
import com.athletetracker.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PerformanceMetricService(
    private val performanceMetricRepository: PerformanceMetricRepository,
    private val athleteRepository: AthleteRepository,
    private val userRepository: UserRepository
) {
    
    fun createPerformanceMetric(request: CreatePerformanceMetricRequest, recordedById: Long? = null): PerformanceMetricResponse {
        val athlete = athleteRepository.findByIdOrNull(request.athleteId)
            ?: throw IllegalArgumentException("Athlete not found with id: ${request.athleteId}")
        
        val recordedBy = recordedById?.let { 
            userRepository.findByIdOrNull(it)
                ?: throw IllegalArgumentException("User not found with id: $it")
        }
        
        val performanceMetric = PerformanceMetric(
            athlete = athlete,
            metricType = request.metricType,
            metricValue = request.metricValue,
            unit = request.unit,
            testDate = request.testDate,
            notes = request.notes,
            recordedBy = recordedBy
        )
        
        val savedMetric = performanceMetricRepository.save(performanceMetric)
        return mapToResponse(savedMetric)
    }
    
    @Transactional(readOnly = true)
    fun getMetricsByAthlete(athleteId: Long): List<PerformanceMetricResponse> {
        val athlete = athleteRepository.findByIdOrNull(athleteId)
            ?: throw IllegalArgumentException("Athlete not found with id: $athleteId")
        
        return performanceMetricRepository.findByAthleteOrderByTestDateDesc(athlete)
            .map { mapToResponse(it) }
    }
    
    @Transactional(readOnly = true)
    fun getMetricsByAthleteAndType(athleteId: Long, metricType: MetricType): List<PerformanceMetricResponse> {
        val athlete = athleteRepository.findByIdOrNull(athleteId)
            ?: throw IllegalArgumentException("Athlete not found with id: $athleteId")
        
        return performanceMetricRepository.findByAthleteAndMetricTypeOrderByTestDateDesc(athlete, metricType)
            .map { mapToResponse(it) }
    }
    
    @Transactional(readOnly = true)
    fun getMetricProgress(athleteId: Long, metricType: MetricType): MetricProgressResponse {
        val athlete = athleteRepository.findByIdOrNull(athleteId)
            ?: throw IllegalArgumentException("Athlete not found with id: $athleteId")
        
        val metrics = performanceMetricRepository.findByAthleteAndMetricTypeOrderByTestDateDesc(athlete, metricType)
        val currentMetric = metrics.firstOrNull()
        val previousMetric = metrics.drop(1).firstOrNull()
        
        val improvement = if (currentMetric != null && previousMetric != null) {
            currentMetric.metricValue - previousMetric.metricValue
        } else null
        
        val improvementPercentage = if (improvement != null && previousMetric != null && previousMetric.metricValue != 0.0) {
            (improvement / previousMetric.metricValue) * 100
        } else null
        
        return MetricProgressResponse(
            metricType = metricType,
            currentValue = currentMetric?.metricValue,
            previousValue = previousMetric?.metricValue,
            improvement = improvement,
            improvementPercentage = improvementPercentage,
            history = metrics.map { mapToResponse(it) }
        )
    }
    
    @Transactional(readOnly = true)
    fun getMetricById(id: Long): PerformanceMetricResponse {
        val metric = performanceMetricRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Performance metric not found with id: $id")
        return mapToResponse(metric)
    }
    
    fun updatePerformanceMetric(id: Long, request: CreatePerformanceMetricRequest): PerformanceMetricResponse {
        val existingMetric = performanceMetricRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Performance metric not found with id: $id")
        
        val athlete = athleteRepository.findByIdOrNull(request.athleteId)
            ?: throw IllegalArgumentException("Athlete not found with id: ${request.athleteId}")
        
        val updatedMetric = existingMetric.copy(
            athlete = athlete,
            metricType = request.metricType,
            metricValue = request.metricValue,
            unit = request.unit,
            testDate = request.testDate,
            notes = request.notes
        )
        
        val savedMetric = performanceMetricRepository.save(updatedMetric)
        return mapToResponse(savedMetric)
    }
    
    fun deletePerformanceMetric(id: Long) {
        if (!performanceMetricRepository.existsById(id)) {
            throw IllegalArgumentException("Performance metric not found with id: $id")
        }
        performanceMetricRepository.deleteById(id)
    }
    
    @Transactional(readOnly = true)
    fun getAthleteMetricsSummary(athleteId: Long): Map<MetricType, PerformanceMetricResponse> {
        val athlete = athleteRepository.findByIdOrNull(athleteId)
            ?: throw IllegalArgumentException("Athlete not found with id: $athleteId")
        
        return MetricType.entries.mapNotNull { metricType ->
            performanceMetricRepository.findLatestByAthleteAndMetricType(athlete, metricType)?.let { metric ->
                metricType to mapToResponse(metric)
            }
        }.toMap()
    }
    
    @Transactional(readOnly = true)
    fun getMetricsInDateRange(
        athleteId: Long, 
        metricType: MetricType, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): List<PerformanceMetricResponse> {
        val athlete = athleteRepository.findByIdOrNull(athleteId)
            ?: throw IllegalArgumentException("Athlete not found with id: $athleteId")
        
        return performanceMetricRepository.findByAthleteAndMetricTypeAndDateRange(
            athlete, metricType, startDate, endDate
        ).map { mapToResponse(it) }
    }
    
    private fun mapToResponse(metric: PerformanceMetric): PerformanceMetricResponse {
        return PerformanceMetricResponse(
            id = metric.id,
            metricType = metric.metricType,
            metricValue = metric.metricValue,
            unit = metric.unit,
            testDate = metric.testDate,
            notes = metric.notes,
            recordedBy = metric.recordedBy?.fullName
        )
    }
}