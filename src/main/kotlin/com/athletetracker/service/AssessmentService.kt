package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.*
import com.athletetracker.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class AssessmentService(
    private val assessmentRepository: AssessmentRepository,
    private val assessmentResultRepository: AssessmentResultRepository,
    private val assessmentScheduleRepository: AssessmentScheduleRepository,
    private val athleteRepository: AthleteRepository,
    private val userRepository: UserRepository,
    private val performanceIntegrationService: PerformanceIntegrationService
) {

    // Assessment CRUD operations
    fun createAssessment(request: CreateAssessmentRequest): Assessment {
        val createdBy = userRepository.findById(request.createdById)
            .orElseThrow { IllegalArgumentException("User not found with id: ${request.createdById}") }

        val assessment = Assessment(
            name = request.name,
            description = request.description,
            category = request.category,
            type = request.type,
            instructions = request.instructions,
            unit = request.unit,
            scoringType = request.scoringType,
            targetValue = request.targetValue,
            minValue = request.minValue,
            maxValue = request.maxValue,
            equipmentRequired = request.equipmentRequired,
            estimatedDuration = request.estimatedDuration,
            sport = request.sport,
            createdBy = createdBy,
            isTemplate = request.isTemplate
        )

        return assessmentRepository.save(assessment)
    }

    fun getAssessmentById(id: Long): Assessment {
        return assessmentRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Assessment not found with id: $id") }
    }

    fun getAllAssessments(): List<Assessment> {
        return assessmentRepository.findByIsActiveTrue()
    }

    fun getAssessmentsByCategory(category: AssessmentCategory): List<Assessment> {
        return assessmentRepository.findByCategoryAndIsActiveTrue(category)
    }

    fun getAssessmentsBySport(sport: Sport): List<Assessment> {
        return assessmentRepository.findBySportAndIsActiveTrue(sport)
    }

    fun getAssessmentTemplates(): List<Assessment> {
        return assessmentRepository.findByIsTemplateAndIsActiveTrue(true)
    }

    fun searchAssessments(query: String): List<Assessment> {
        return assessmentRepository.searchByNameOrDescription(query)
    }

    fun updateAssessment(id: Long, request: UpdateAssessmentRequest): Assessment {
        val assessment = getAssessmentById(id)
        
        val updatedAssessment = assessment.copy(
            name = request.name ?: assessment.name,
            description = request.description ?: assessment.description,
            instructions = request.instructions ?: assessment.instructions,
            unit = request.unit ?: assessment.unit,
            scoringType = request.scoringType ?: assessment.scoringType,
            targetValue = request.targetValue ?: assessment.targetValue,
            minValue = request.minValue ?: assessment.minValue,
            maxValue = request.maxValue ?: assessment.maxValue,
            equipmentRequired = request.equipmentRequired ?: assessment.equipmentRequired,
            estimatedDuration = request.estimatedDuration ?: assessment.estimatedDuration,
            isActive = request.isActive ?: assessment.isActive,
            updatedAt = LocalDateTime.now()
        )

        return assessmentRepository.save(updatedAssessment)
    }

    fun deleteAssessment(id: Long) {
        val assessment = getAssessmentById(id)
        val deactivatedAssessment = assessment.copy(isActive = false, updatedAt = LocalDateTime.now())
        assessmentRepository.save(deactivatedAssessment)
    }

    // Assessment Result operations
    fun recordAssessmentResult(request: CreateAssessmentResultRequest): AssessmentResult {
        val assessment = getAssessmentById(request.assessmentId)
        val athlete = athleteRepository.findById(request.athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: ${request.athleteId}") }
        
        val conductedBy = request.conductedById?.let { 
            userRepository.findById(it)
                .orElseThrow { IllegalArgumentException("User not found with id: $it") }
        }

        // Check for existing result to prevent duplicates
        val existingResult = assessmentResultRepository.findByAssessmentIdAndAthleteIdAndTestDate(
            request.assessmentId, request.athleteId, request.testDate
        )
        if (existingResult != null) {
            throw IllegalArgumentException("Assessment result already exists for this athlete, assessment, and date")
        }

        // Calculate improvements if baseline exists
        val baseline = if (!request.isBaseline) {
            assessmentResultRepository.findBaselineResultsByAthleteId(request.athleteId)
                .find { it.assessment.id == request.assessmentId }
        } else null

        val improvementFromBaseline = baseline?.let { request.value - it.value }
        val improvementPercentage = baseline?.let { 
            if (it.value != 0.0) ((request.value - it.value) / it.value) * 100 else null 
        }

        val result = AssessmentResult(
            assessment = assessment,
            athlete = athlete,
            testDate = request.testDate,
            value = request.value,
            rawValue = request.rawValue,
            notes = request.notes,
            conditions = request.conditions,
            isBaseline = request.isBaseline,
            videoUrl = request.videoUrl,
            conductedBy = conductedBy,
            improvementFromBaseline = improvementFromBaseline,
            improvementPercentage = improvementPercentage
        )

        val savedResult = assessmentResultRepository.save(result)

        // Find and update corresponding assessment schedule(s) to COMPLETED status
        val correspondingSchedules = assessmentScheduleRepository.findByAthleteAndAssessmentAndDate(
            athlete, assessment, request.testDate
        )
        correspondingSchedules.forEach { schedule: AssessmentSchedule ->
            if (schedule.status != ScheduleStatus.COMPLETED) {
                assessmentScheduleRepository.save(schedule.copy(status = ScheduleStatus.COMPLETED))
            }
        }

        // Auto-generate performance metric if assessment is configured to do so
        val metricType = performanceIntegrationService.mapAssessmentToMetricType(assessment)
        if (metricType != null) {
            try {
                performanceIntegrationService.createMetricFromAssessment(savedResult, metricType)
            } catch (e: Exception) {
                // Log the error but don't fail the assessment result creation
                // In a real application, you'd use proper logging
                println("Failed to create performance metric from assessment result: ${e.message}")
            }
        }

        return savedResult
    }

    fun getAssessmentResultById(id: Long): AssessmentResult {
        return assessmentResultRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Assessment result not found with id: $id") }
    }

    fun getResultsByAthlete(athleteId: Long): List<AssessmentResult> {
        return assessmentResultRepository.findByAthleteIdOrderByTestDateDesc(athleteId)
    }

    fun getResultsByAssessment(assessmentId: Long): List<AssessmentResult> {
        return assessmentResultRepository.findByAssessmentIdOrderByTestDateDesc(assessmentId)
    }

    fun getAssessmentProgress(athleteId: Long, assessmentId: Long): AssessmentProgressResponse {
        val assessment = getAssessmentById(assessmentId)
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }

        val results = assessmentResultRepository.findProgressByAthleteAndAssessment(athleteId, assessmentId)
        val baseline = results.find { it.isBaseline }
        val latest = results.lastOrNull()
        
        // Determine best result based on scoring type
        val best = when (assessment.scoringType) {
            "lower_better" -> results.minByOrNull { it.value }
            "higher_better" -> results.maxByOrNull { it.value }
            else -> results.maxByOrNull { it.value } // Default to higher is better
        }

        val totalImprovement = if (baseline != null && latest != null) {
            latest.value - baseline.value
        } else null

        val totalImprovementPercentage = if (baseline != null && latest != null && baseline.value != 0.0) {
            ((latest.value - baseline.value) / baseline.value) * 100
        } else null

        // Determine trend (simple logic based on last 3 results)
        val trend = if (results.size >= 3) {
            val recent = results.takeLast(3)
            val isImproving = when (assessment.scoringType) {
                "lower_better" -> recent.zipWithNext().all { (prev, next) -> next.value <= prev.value }
                "higher_better" -> recent.zipWithNext().all { (prev, next) -> next.value >= prev.value }
                else -> recent.zipWithNext().all { (prev, next) -> next.value >= prev.value }
            }
            if (isImproving) "improving" else "stable"
        } else "stable"

        val chartData = results.map { result ->
            AssessmentChartDataPoint(
                date = result.testDate,
                value = result.value,
                label = result.rawValue,
                isBaseline = result.isBaseline
            )
        }

        return AssessmentProgressResponse(
            assessment = convertToBasicDto(assessment),
            athlete = convertToBasicDto(athlete),
            results = results.map { convertToDto(it) },
            baseline = baseline?.let { convertToDto(it) },
            latest = latest?.let { convertToDto(it) },
            best = best?.let { convertToDto(it) },
            totalImprovement = totalImprovement,
            totalImprovementPercentage = totalImprovementPercentage,
            trend = trend,
            chartData = chartData
        )
    }

    // Assessment Scheduling operations
    fun scheduleAssessment(request: CreateAssessmentScheduleRequest): AssessmentSchedule {
        val assessment = getAssessmentById(request.assessmentId)
        val athlete = athleteRepository.findById(request.athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: ${request.athleteId}") }
        val scheduledBy = userRepository.findById(request.scheduledById)
            .orElseThrow { IllegalArgumentException("User not found with id: ${request.scheduledById}") }

        val schedule = AssessmentSchedule(
            assessment = assessment,
            athlete = athlete,
            scheduledDate = request.scheduledDate,
            scheduledTime = request.scheduledTime,
            recurrenceType = request.recurrenceType,
            recurrenceInterval = request.recurrenceInterval,
            recurrenceEndDate = request.recurrenceEndDate,
            maxRecurrences = request.maxRecurrences,
            notes = request.notes,
            specialInstructions = request.specialInstructions,
            location = request.location,
            scheduledBy = scheduledBy
        )

        return assessmentScheduleRepository.save(schedule)
    }

    fun getUpcomingAssessments(): List<AssessmentSchedule> {
        return assessmentScheduleRepository.findUpcomingAssessments(LocalDate.now())
    }

    fun getScheduledAssessmentsForAthlete(athleteId: Long): List<AssessmentScheduleDto> {
        val schedules = assessmentScheduleRepository.findByAthleteIdAndIsActiveTrue(athleteId)
        return schedules.map { convertToDto(it) }
    }

    fun getAthleteAssessmentSummary(athleteId: Long): AthleteAssessmentSummaryDto {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }

        val totalResults = assessmentResultRepository.countCompletedAssessmentsByAthleteId(athleteId)
        val totalSchedules = assessmentScheduleRepository.countByAthleteIdAndIsActiveTrue(athleteId)
        
        val upcomingSchedules = assessmentScheduleRepository.findUpcomingAssessmentsForAthlete(
            athleteId, LocalDate.now(), LocalDate.now().plusDays(30)
        )
        
        val recentResults = assessmentResultRepository.findRecentResultsByAthleteId(
            athleteId, LocalDate.now().minusDays(30)
        ).take(5)

        val lastAssessmentDate = recentResults.maxOfOrNull { it.testDate }

        return AthleteAssessmentSummaryDto(
            athlete = convertToBasicDto(athlete),
            totalAssessments = assessmentRepository.findByIsActiveTrue().size.toLong(),
            completedAssessments = totalResults,
            upcomingAssessments = upcomingSchedules.size.toLong(),
            lastAssessmentDate = lastAssessmentDate,
            recentResults = recentResults.map { convertToDto(it) },
            upcomingSchedules = upcomingSchedules.map { convertToDto(it) }
        )
    }

    // Initialize default assessments
    fun initializeDefaultAssessments() {
        if (assessmentRepository.count() > 0) return

        val adminUser = userRepository.findAll().find { it.role == UserRole.ADMIN }
            ?: throw IllegalStateException("No admin user found for creating default assessments")

        val defaultAssessments = listOf(
            // Strength Tests
            Triple("Bench Press 1RM", AssessmentCategory.STRENGTH_TEST, "lbs"),
            Triple("Squat 1RM", AssessmentCategory.STRENGTH_TEST, "lbs"),
            Triple("Deadlift 1RM", AssessmentCategory.STRENGTH_TEST, "lbs"),
            Triple("Push-up Test", AssessmentCategory.FITNESS_TEST, "reps"),
            
            // Speed & Agility
            Triple("40-Yard Dash", AssessmentCategory.SPEED_AGILITY, "seconds"),
            Triple("20-Yard Dash", AssessmentCategory.SPEED_AGILITY, "seconds"),
            Triple("Three Cone Drill", AssessmentCategory.SPEED_AGILITY, "seconds"),
            Triple("Shuttle Run", AssessmentCategory.SPEED_AGILITY, "seconds"),
            
            // Power Tests
            Triple("Vertical Jump", AssessmentCategory.FITNESS_TEST, "inches"),
            Triple("Broad Jump", AssessmentCategory.FITNESS_TEST, "inches"),
            
            // Endurance
            Triple("Mile Run", AssessmentCategory.ENDURANCE, "minutes"),
            Triple("VO2 Max Test", AssessmentCategory.ENDURANCE, "ml/kg/min"),
            
            // Flexibility
            Triple("Sit and Reach", AssessmentCategory.FLEXIBILITY_MOBILITY, "inches"),
            Triple("Shoulder Flexibility", AssessmentCategory.FLEXIBILITY_MOBILITY, "degrees"),
            
            // Body Composition
            Triple("Body Weight", AssessmentCategory.BODY_COMPOSITION, "lbs"),
            Triple("Body Fat Percentage", AssessmentCategory.BODY_COMPOSITION, "%")
        )

        defaultAssessments.forEach { (name, category, unit) ->
            val assessment = Assessment(
                name = name,
                description = "Standard $name assessment",
                category = category,
                type = when (category) {
                    AssessmentCategory.SPEED_AGILITY, AssessmentCategory.ENDURANCE -> AssessmentType.TIMED
                    AssessmentCategory.STRENGTH_TEST -> AssessmentType.WEIGHT
                    AssessmentCategory.FITNESS_TEST -> if (unit == "reps") AssessmentType.REPETITION else AssessmentType.DISTANCE
                    AssessmentCategory.BODY_COMPOSITION -> if (unit == "%") AssessmentType.PERCENTAGE else AssessmentType.WEIGHT
                    else -> AssessmentType.MEASUREMENT
                },
                unit = unit,
                scoringType = when (category) {
                    AssessmentCategory.SPEED_AGILITY, AssessmentCategory.ENDURANCE -> "lower_better"
                    else -> "higher_better"
                },
                sport = Sport.BASEBALL, // Default sport
                createdBy = adminUser,
                isTemplate = true
            )
            assessmentRepository.save(assessment)
        }
    }

    // Conversion functions
    private fun convertToDto(assessment: Assessment): AssessmentDto {
        return AssessmentDto(
            id = assessment.id,
            name = assessment.name,
            description = assessment.description,
            category = assessment.category,
            type = assessment.type,
            instructions = assessment.instructions,
            unit = assessment.unit,
            scoringType = assessment.scoringType,
            targetValue = assessment.targetValue,
            minValue = assessment.minValue,
            maxValue = assessment.maxValue,
            equipmentRequired = assessment.equipmentRequired,
            estimatedDuration = assessment.estimatedDuration,
            sport = assessment.sport,
            createdBy = UserBasicDto(
                id = assessment.createdBy.id,
                firstName = assessment.createdBy.firstName,
                lastName = assessment.createdBy.lastName,
                email = assessment.createdBy.email
            ),
            isActive = assessment.isActive,
            isTemplate = assessment.isTemplate,
            createdAt = assessment.createdAt,
            updatedAt = assessment.updatedAt
        )
    }

    private fun convertToBasicDto(assessment: Assessment): AssessmentBasicDto {
        return AssessmentBasicDto(
            id = assessment.id,
            name = assessment.name,
            category = assessment.category,
            type = assessment.type,
            unit = assessment.unit,
            sport = assessment.sport
        )
    }

    private fun convertToDto(result: AssessmentResult): AssessmentResultDto {
        return AssessmentResultDto(
            id = result.id,
            assessment = convertToBasicDto(result.assessment),
            athlete = AthleteBasicDto(
                id = result.athlete.id,
                firstName = result.athlete.firstName,
                lastName = result.athlete.lastName,
                dateOfBirth = result.athlete.dateOfBirth,
                sport = result.athlete.sport.toString()
            ),
            testDate = result.testDate,
            value = result.value,
            rawValue = result.rawValue,
            status = result.status,
            notes = result.notes,
            conditions = result.conditions,
            percentileRank = result.percentileRank,
            improvementFromBaseline = result.improvementFromBaseline,
            improvementPercentage = result.improvementPercentage,
            isBaseline = result.isBaseline,
            videoUrl = result.videoUrl,
            conductedBy = result.conductedBy?.let {
                UserBasicDto(it.id, it.firstName, it.lastName, it.email)
            },
            createdAt = result.createdAt
        )
    }

    private fun convertToDto(schedule: AssessmentSchedule): AssessmentScheduleDto {
        return AssessmentScheduleDto(
            id = schedule.id,
            assessment = convertToBasicDto(schedule.assessment),
            athlete = AthleteBasicDto(
                id = schedule.athlete.id,
                firstName = schedule.athlete.firstName,
                lastName = schedule.athlete.lastName,
                dateOfBirth = schedule.athlete.dateOfBirth,
                sport = schedule.athlete.sport.toString()
            ),
            scheduledDate = schedule.scheduledDate,
            scheduledTime = schedule.scheduledTime,
            status = schedule.status,
            recurrenceType = schedule.recurrenceType,
            recurrenceInterval = schedule.recurrenceInterval,
            recurrenceEndDate = schedule.recurrenceEndDate,
            maxRecurrences = schedule.maxRecurrences,
            notes = schedule.notes,
            specialInstructions = schedule.specialInstructions,
            location = schedule.location,
            reminderSent = schedule.reminderSent,
            reminderDate = schedule.reminderDate,
            scheduledBy = UserBasicDto(
                id = schedule.scheduledBy.id,
                firstName = schedule.scheduledBy.firstName,
                lastName = schedule.scheduledBy.lastName,
                email = schedule.scheduledBy.email
            ),
            createdAt = schedule.createdAt
        )
    }

    private fun convertToBasicDto(athlete: Athlete): AthleteBasicDto {
        return AthleteBasicDto(
            id = athlete.id,
            firstName = athlete.firstName,
            lastName = athlete.lastName,
            dateOfBirth = athlete.dateOfBirth,
            sport = athlete.sport.toString()
        )
    }
}