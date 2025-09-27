package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.MetricType
import com.athletetracker.service.PerformanceMetricService
import com.athletetracker.service.PersonalRecordService
import com.athletetracker.service.PRTimelineEntry
import com.athletetracker.repository.AthleteRepository
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/performance-metrics")
class PerformanceMetricController(
    private val performanceMetricService: PerformanceMetricService,
    private val personalRecordService: PersonalRecordService,
    private val athleteRepository: AthleteRepository
) {
    
    @PostMapping
    fun createPerformanceMetric(
        @Valid @RequestBody request: CreatePerformanceMetricRequest,
        @RequestParam(required = false) recordedById: Long?
    ): ResponseEntity<PerformanceMetricResponse> {
        val metric = performanceMetricService.createPerformanceMetric(request, recordedById)
        return ResponseEntity(metric, HttpStatus.CREATED)
    }
    
    @GetMapping("/{id}")
    fun getPerformanceMetricById(@PathVariable id: Long): ResponseEntity<PerformanceMetricResponse> {
        val metric = performanceMetricService.getMetricById(id)
        return ResponseEntity.ok(metric)
    }
    
    @PutMapping("/{id}")
    fun updatePerformanceMetric(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreatePerformanceMetricRequest
    ): ResponseEntity<PerformanceMetricResponse> {
        val metric = performanceMetricService.updatePerformanceMetric(id, request)
        return ResponseEntity.ok(metric)
    }
    
    @DeleteMapping("/{id}")
    fun deletePerformanceMetric(@PathVariable id: Long): ResponseEntity<Void> {
        performanceMetricService.deletePerformanceMetric(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/athlete/{athleteId}")
    fun getMetricsByAthlete(@PathVariable athleteId: Long): ResponseEntity<List<PerformanceMetricResponse>> {
        val metrics = performanceMetricService.getMetricsByAthlete(athleteId)
        return ResponseEntity.ok(metrics)
    }
    
    @GetMapping("/athlete/{athleteId}/type/{metricType}")
    fun getMetricsByAthleteAndType(
        @PathVariable athleteId: Long,
        @PathVariable metricType: MetricType
    ): ResponseEntity<List<PerformanceMetricResponse>> {
        val metrics = performanceMetricService.getMetricsByAthleteAndType(athleteId, metricType)
        return ResponseEntity.ok(metrics)
    }
    
    @GetMapping("/athlete/{athleteId}/progress/{metricType}")
    fun getMetricProgress(
        @PathVariable athleteId: Long,
        @PathVariable metricType: MetricType
    ): ResponseEntity<MetricProgressResponse> {
        val progress = performanceMetricService.getMetricProgress(athleteId, metricType)
        return ResponseEntity.ok(progress)
    }
    
    @GetMapping("/athlete/{athleteId}/summary")
    fun getAthleteMetricsSummary(@PathVariable athleteId: Long): ResponseEntity<Map<MetricType, PerformanceMetricResponse>> {
        val summary = performanceMetricService.getAthleteMetricsSummary(athleteId)
        return ResponseEntity.ok(summary)
    }
    
    @GetMapping("/athlete/{athleteId}/type/{metricType}/range")
    fun getMetricsInDateRange(
        @PathVariable athleteId: Long,
        @PathVariable metricType: MetricType,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<List<PerformanceMetricResponse>> {
        val metrics = performanceMetricService.getMetricsInDateRange(athleteId, metricType, startDate, endDate)
        return ResponseEntity.ok(metrics)
    }
    
    @GetMapping("/types")
    fun getAvailableMetricTypes(): ResponseEntity<List<MetricType>> {
        return ResponseEntity.ok(MetricType.values().toList())
    }
    
    // New unified performance metrics endpoints
    @GetMapping("/unified/{athleteId}")
    fun getUnifiedPerformanceMetrics(@PathVariable athleteId: Long): ResponseEntity<List<PerformanceMetricResponse>> {
        val metrics = performanceMetricService.getMetricsByAthlete(athleteId)
        return ResponseEntity.ok(metrics)
    }
    
    @GetMapping("/prs/{athleteId}")
    fun getPersonalRecords(@PathVariable athleteId: Long): ResponseEntity<List<PRTimelineEntry>> {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        val prTimeline = personalRecordService.getPRTimeline(athlete)
        return ResponseEntity.ok(prTimeline)
    }
    
    @GetMapping("/athlete/{athleteId}/prs")
    fun getAthletePersonalRecords(@PathVariable athleteId: Long): ResponseEntity<List<PerformanceMetricResponse>> {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        val personalRecords = personalRecordService.getPersonalRecords(athlete)
        val prResponses = personalRecords.map { metric ->
            PerformanceMetricResponse(
                id = metric.id,
                metricType = metric.metricType,
                metricValue = metric.metricValue,
                unit = metric.unit,
                testDate = metric.testDate,
                notes = metric.notes,
                recordedBy = metric.recordedBy?.fullName
            )
        }
        return ResponseEntity.ok(prResponses)
    }
}