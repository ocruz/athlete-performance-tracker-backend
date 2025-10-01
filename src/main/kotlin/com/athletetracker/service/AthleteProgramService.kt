package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.*
import com.athletetracker.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
@Transactional
class AthleteProgramService(
    private val athleteProgramRepository: AthleteProgramRepository,
    private val programProgressRepository: ProgramProgressRepository,
    private val athleteRepository: AthleteRepository,
    private val programRepository: ProgramRepository,
    private val userRepository: UserRepository,
    private val athleteWorkoutExerciseRepository: AthleteWorkoutExerciseRepository,
) {

    fun assignProgram(request: AssignProgramRequest, assignedById: Long): AthleteProgramResponse {
        val athlete = athleteRepository.findById(request.athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: ${request.athleteId}") }
        
        val program = programRepository.findById(request.programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: ${request.programId}") }
        
        val assignedBy = userRepository.findById(assignedById)
            .orElseThrow { IllegalArgumentException("User not found with id: $assignedById") }

        // Check if athlete already has an active program
        val activePrograms = athleteProgramRepository.findActiveByAthleteId(request.athleteId)
        if (activePrograms.isNotEmpty()) {
            throw IllegalStateException("Athlete already has active programs. Please complete or pause current programs first.")
        }

        val athleteProgram = AthleteProgram(
            athlete = athlete,
            program = program,
            assignedBy = assignedBy,
            startDate = request.startDate,
            endDate = request.endDate,
            notes = request.notes
        )

        val savedAthleteProgram = athleteProgramRepository.save(athleteProgram)
        return convertToResponse(savedAthleteProgram)
    }

    fun getAthletePrograms(athleteId: Long): List<AthleteProgramResponse> {
        val athletePrograms = athleteProgramRepository.findByAthleteId(athleteId)
        return athletePrograms.map { convertToResponse(it) }
    }

    fun getActiveProgramsForAthlete(athleteId: Long): List<AthleteProgramResponse> {
        val activePrograms = athleteProgramRepository.findActiveByAthleteId(athleteId)
        return activePrograms.map { convertToResponse(it) }
    }

    fun getProgramsByCoach(coachId: Long): List<AthleteProgramResponse> {
        val programs = athleteProgramRepository.findByCoachId(coachId)
        return programs.map { convertToResponse(it) }
    }

    fun updateProgramStatus(athleteProgramId: Long, request: UpdateProgramStatusRequest): AthleteProgramResponse {
        val athleteProgram = athleteProgramRepository.findById(athleteProgramId)
            .orElseThrow { IllegalArgumentException("Athlete program not found with id: $athleteProgramId") }

        val updatedAthleteProgram = athleteProgram.copy(
            status = request.status,
            notes = request.notes ?: athleteProgram.notes,
            updatedAt = LocalDateTime.now()
        )

        val savedAthleteProgram = athleteProgramRepository.save(updatedAthleteProgram)
        return convertToResponse(savedAthleteProgram)
    }

    fun getAthleteProgramDetail(athleteProgramId: Long): AthleteProgramResponse {
        val athleteProgram = athleteProgramRepository.findById(athleteProgramId)
            .orElseThrow { IllegalArgumentException("Athlete program not found with id: $athleteProgramId") }
        
        return convertToResponse(athleteProgram)
    }

    private fun convertToResponse(athleteProgram: AthleteProgram): AthleteProgramResponse {
        val progressSummary = calculateProgressSummary(athleteProgram)
        
        return AthleteProgramResponse(
            id = athleteProgram.id,
            athlete = AthleteBasicDto(
                id = athleteProgram.athlete.id,
                firstName = athleteProgram.athlete.firstName,
                lastName = athleteProgram.athlete.lastName,
                dateOfBirth = athleteProgram.athlete.dateOfBirth,
                sport = athleteProgram.athlete.sport.name
            ),
            program = ProgramBasicDto(
                id = athleteProgram.program.id,
                name = athleteProgram.program.name,
                description = athleteProgram.program.description,
                sport = athleteProgram.program.sport.name,
                durationWeeks = athleteProgram.program.durationWeeks,
                difficultyLevel = athleteProgram.program.difficultyLevel
            ),
            assignedBy = UserBasicDto(
                id = athleteProgram.assignedBy.id,
                firstName = athleteProgram.assignedBy.firstName,
                lastName = athleteProgram.assignedBy.lastName,
                email = athleteProgram.assignedBy.email
            ),
            startDate = athleteProgram.startDate,
            endDate = athleteProgram.endDate,
            status = athleteProgram.status,
            notes = athleteProgram.notes,
            createdAt = athleteProgram.createdAt,
            progressSummary = progressSummary
        )
    }

    private fun calculateProgressSummary(athleteProgram: AthleteProgram): ProgramProgressSummary {
        val totalExercises = athleteProgram.program.programWorkouts.sumOf { it.exercises.size }
        // Use actual athlete workout exercise completions instead of program progress tracking
        val completedExercises = athleteWorkoutExerciseRepository.countCompletedExercisesByAthleteProgram(athleteProgram.id)
        val completionPercentage = if (totalExercises > 0) {
            (completedExercises.toDouble() / totalExercises.toDouble()) * 100
        } else 0.0

        // Calculate current week based on start date
        val daysSinceStart = ChronoUnit.DAYS.between(athleteProgram.startDate, java.time.LocalDate.now())
        val currentWeek = if (daysSinceStart >= 0) ((daysSinceStart / 7) + 1).toInt() else null

        // Get last activity date from actual workout completions (keeping program progress for now as fallback)
        val progressEntries = programProgressRepository.findByAthleteProgramId(athleteProgram.id)
        val lastActivityDate = progressEntries.maxByOrNull { it.completedDate }?.completedDate

        // Calculate adherence rate based on actual workout completions
        val adherenceRate = if (totalExercises > 0) {
            (completedExercises.toDouble() / totalExercises.toDouble()) * 100
        } else 0.0

        return ProgramProgressSummary(
            totalExercises = totalExercises,
            completedExercises = completedExercises.toInt(),
            completionPercentage = completionPercentage,
            currentWeek = currentWeek,
            lastActivityDate = lastActivityDate,
            adherenceRate = adherenceRate
        )
    }
}

// Request DTOs for workout scheduling
data class ScheduleWorkoutsForWeekRequest(
    val athleteProgramId: Long,
    val coachId: Long,
    val weekNumber: Int,
    val startDate: java.time.LocalDate,
    val notes: String? = null
)

data class ScheduleWorkoutForDayRequest(
    val athleteProgramId: Long,
    val coachId: Long,
    val weekNumber: Int,
    val dayNumber: Int,
    val workoutDate: LocalDateTime,
    val workoutName: String? = null,
    val notes: String? = null
)

data class AutoScheduleProgramRequest(
    val athleteProgramId: Long,
    val coachId: Long,
    val skipWeekends: Boolean = true,
    val notes: String? = null
)