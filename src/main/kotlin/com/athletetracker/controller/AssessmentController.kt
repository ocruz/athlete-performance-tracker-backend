package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.*
import com.athletetracker.service.AssessmentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/assessments")
@CrossOrigin(origins = ["http://localhost:3000"])
class AssessmentController(
    private val assessmentService: AssessmentService
) {

    // Assessment CRUD endpoints
    @GetMapping
    fun getAllAssessments(): ResponseEntity<List<Assessment>> {
        val assessments = assessmentService.getAllAssessments()
        return ResponseEntity.ok(assessments)
    }

    @GetMapping("/{id}")
    fun getAssessmentById(@PathVariable id: Long): ResponseEntity<Assessment> {
        val assessment = assessmentService.getAssessmentById(id)
        return ResponseEntity.ok(assessment)
    }

    @PostMapping
    fun createAssessment(@RequestBody request: CreateAssessmentRequest): ResponseEntity<Assessment> {
        val assessment = assessmentService.createAssessment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(assessment)
    }

    @PutMapping("/{id}")
    fun updateAssessment(
        @PathVariable id: Long,
        @RequestBody request: UpdateAssessmentRequest
    ): ResponseEntity<Assessment> {
        val assessment = assessmentService.updateAssessment(id, request)
        return ResponseEntity.ok(assessment)
    }

    @DeleteMapping("/{id}")
    fun deleteAssessment(@PathVariable id: Long): ResponseEntity<Void> {
        assessmentService.deleteAssessment(id)
        return ResponseEntity.noContent().build()
    }

    // Assessment filtering and search endpoints
    @GetMapping("/category/{category}")
    fun getAssessmentsByCategory(@PathVariable category: AssessmentCategory): ResponseEntity<List<Assessment>> {
        val assessments = assessmentService.getAssessmentsByCategory(category)
        return ResponseEntity.ok(assessments)
    }

    @GetMapping("/sport/{sport}")
    fun getAssessmentsBySport(@PathVariable sport: Sport): ResponseEntity<List<Assessment>> {
        val assessments = assessmentService.getAssessmentsBySport(sport)
        return ResponseEntity.ok(assessments)
    }

    @GetMapping("/templates")
    fun getAssessmentTemplates(): ResponseEntity<List<Assessment>> {
        val templates = assessmentService.getAssessmentTemplates()
        return ResponseEntity.ok(templates)
    }

    @GetMapping("/search")
    fun searchAssessments(@RequestParam query: String): ResponseEntity<List<Assessment>> {
        val assessments = assessmentService.searchAssessments(query)
        return ResponseEntity.ok(assessments)
    }

    // Assessment Results endpoints
    @PostMapping("/results")
    fun recordAssessmentResult(@RequestBody request: CreateAssessmentResultRequest): ResponseEntity<AssessmentResult> {
        val result = assessmentService.recordAssessmentResult(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @GetMapping("/results/{id}")
    fun getAssessmentResultById(@PathVariable id: Long): ResponseEntity<AssessmentResult> {
        val result = assessmentService.getAssessmentResultById(id)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/results/athlete/{athleteId}")
    fun getResultsByAthlete(@PathVariable athleteId: Long): ResponseEntity<List<AssessmentResult>> {
        val results = assessmentService.getResultsByAthlete(athleteId)
        return ResponseEntity.ok(results)
    }

    @GetMapping("/results/assessment/{assessmentId}")
    fun getResultsByAssessment(@PathVariable assessmentId: Long): ResponseEntity<List<AssessmentResult>> {
        val results = assessmentService.getResultsByAssessment(assessmentId)
        return ResponseEntity.ok(results)
    }

    @GetMapping("/progress/athlete/{athleteId}/assessment/{assessmentId}")
    fun getAssessmentProgress(
        @PathVariable athleteId: Long,
        @PathVariable assessmentId: Long
    ): ResponseEntity<AssessmentProgressResponse> {
        val progress = assessmentService.getAssessmentProgress(athleteId, assessmentId)
        return ResponseEntity.ok(progress)
    }

    // Assessment Scheduling endpoints
    @PostMapping("/schedules")
    fun scheduleAssessment(@RequestBody request: CreateAssessmentScheduleRequest): ResponseEntity<AssessmentSchedule> {
        val schedule = assessmentService.scheduleAssessment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule)
    }

    @GetMapping("/schedules/upcoming")
    fun getUpcomingAssessments(): ResponseEntity<List<AssessmentSchedule>> {
        val schedules = assessmentService.getUpcomingAssessments()
        return ResponseEntity.ok(schedules)
    }

    @GetMapping("/schedules/athlete/{athleteId}")
    fun getScheduledAssessmentsForAthlete(@PathVariable athleteId: Long): ResponseEntity<List<AssessmentScheduleDto>> {
        val schedules = assessmentService.getScheduledAssessmentsForAthlete(athleteId)
        return ResponseEntity.ok(schedules)
    }

    // Assessment Summary and Analytics endpoints
    @GetMapping("/summary/athlete/{athleteId}")
    fun getAthleteAssessmentSummary(@PathVariable athleteId: Long): ResponseEntity<AthleteAssessmentSummaryDto> {
        val summary = assessmentService.getAthleteAssessmentSummary(athleteId)
        return ResponseEntity.ok(summary)
    }

    // Utility endpoints
    @PostMapping("/initialize-defaults")
    fun initializeDefaultAssessments(): ResponseEntity<String> {
        assessmentService.initializeDefaultAssessments()
        return ResponseEntity.ok("Default assessments initialized successfully")
    }

    @GetMapping("/categories")
    fun getAssessmentCategories(): ResponseEntity<List<AssessmentCategory>> {
        return ResponseEntity.ok(AssessmentCategory.values().toList())
    }

    @GetMapping("/types")
    fun getAssessmentTypes(): ResponseEntity<List<AssessmentType>> {
        return ResponseEntity.ok(AssessmentType.values().toList())
    }

    // Exception handling
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("An error occurred: ${e.message}")
    }
}