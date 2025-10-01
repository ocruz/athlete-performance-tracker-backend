package com.athletetracker.service

import com.athletetracker.dto.AthleteBasicDto
import com.athletetracker.dto.ExerciseBasicDto
import com.athletetracker.dto.UserBasicDto
import com.athletetracker.dto.AthleteWorkoutDto
import com.athletetracker.dto.AthleteWorkoutExerciseDto
import com.athletetracker.dto.AthleteWorkoutSummaryDto
import com.athletetracker.dto.ProgramWorkoutBasicDto
import com.athletetracker.dto.ProgramWorkoutExerciseDto
import com.athletetracker.dto.PlannedExerciseDto
import com.athletetracker.dto.ActualExerciseDto
import com.athletetracker.entity.ExerciseCompletionStatus
import com.athletetracker.entity.AthleteWorkout
import com.athletetracker.entity.AthleteWorkoutExercise
import com.athletetracker.repository.AthleteRepository
import com.athletetracker.repository.ExerciseRepository
import com.athletetracker.repository.UserRepository
import com.athletetracker.repository.AthleteWorkoutRepository
import com.athletetracker.repository.AthleteWorkoutExerciseRepository
import com.athletetracker.repository.ProgramWorkoutExerciseRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Transactional
class AthleteProgramWorkoutService(
    private val athleteWorkoutRepository: AthleteWorkoutRepository,
    private val athleteRepository: AthleteRepository,
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository,
    private val athleteWorkoutExerciseRepository: AthleteWorkoutExerciseRepository,
    private val programWorkoutExerciseRepository: ProgramWorkoutExerciseRepository,
    private val personalRecordService: PersonalRecordService,
    private val performanceIntegrationService: PerformanceIntegrationService
) {

    fun createWorkout(request: CreateAthleteWorkoutRequest): AthleteWorkoutDto {
        val athlete = athleteRepository.findById(request.athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: ${request.athleteId}") }
        
        val coach = userRepository.findById(request.coachId)
            .orElseThrow { IllegalArgumentException("Coach not found with id: ${request.coachId}") }

        val athleteWorkout = AthleteWorkout(
            athlete = athlete,
            coach = coach,
            workoutDate = request.workoutDate,
            name = request.name,
            notes = request.notes,
            rpe = request.rpe,
            duration = request.duration
        )

        val savedWorkout = athleteWorkoutRepository.save(athleteWorkout)

        // Handle workout exercises if provided
        if (request.exercises.isNotEmpty()) {
            val athleteWorkoutExercises = request.exercises.mapIndexed { index, exerciseRequest ->
                val exercise = exerciseRepository.findById(exerciseRequest.exerciseId)
                    .orElseThrow { IllegalArgumentException("Exercise not found with id: ${exerciseRequest.exerciseId}") }

                val programWorkoutExercise = exerciseRequest.programWorkoutExerciseId?.let { programWorkoutExerciseId ->
                    programWorkoutExerciseRepository.findById(programWorkoutExerciseId)
                        .orElseThrow { IllegalArgumentException("Program workout exercise not found with id: $programWorkoutExerciseId") }
                }

                AthleteWorkoutExercise(
                    athleteWorkout = savedWorkout,
                    exercise = exercise,
                    programWorkoutExercise = programWorkoutExercise,
                    sets = exerciseRequest.sets,
                    reps = exerciseRequest.reps,
                    weight = exerciseRequest.weight,
                    distance = exerciseRequest.distance,
                    time = exerciseRequest.time,
                    restTime = exerciseRequest.restTime,
                    notes = exerciseRequest.notes,
                    orderInWorkout = index + 1,
                    plannedSets = exerciseRequest.plannedSets,
                    plannedReps = exerciseRequest.plannedReps,
                    plannedIntensity = exerciseRequest.plannedIntensity,
                    isFromProgram = exerciseRequest.isFromProgram
                )
            }

            // Save workout with exercises
            athleteWorkoutExerciseRepository.saveAll(athleteWorkoutExercises)
            return convertToDto(savedWorkout, athleteWorkoutExercises)
        }

        return convertToDto(savedWorkout, emptyList())
    }

    fun getWorkoutById(id: Long): AthleteWorkoutDto {
        return athleteWorkoutRepository.findById(id)
            .map { workout -> 
                val orderedExercises = getOrderedWorkoutExercises(workout)
                convertToDto(workout, orderedExercises) 
            }
            .orElseThrow { IllegalArgumentException("Workout not found with id: $id") }
    }

    /**
     * Helper method to fetch workout exercises in proper order
     */
    private fun getOrderedWorkoutExercises(workout: AthleteWorkout): List<AthleteWorkoutExercise> {
        return athleteWorkoutExerciseRepository.findByAthleteWorkoutOrderByOrderInWorkout(workout)
    }

    fun getAthleteWorkoutsByAthlete(athleteId: Long): List<AthleteWorkoutDto> {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        
        return athleteWorkoutRepository.findByAthleteOrderByWorkoutDateDesc(athlete)
            .map { convertToDto(it, it.athleteWorkoutExercises) }
    }

    fun getWorkoutsByCoach(coachId: Long): List<AthleteWorkoutDto> {
        val coach = userRepository.findById(coachId)
            .orElseThrow { IllegalArgumentException("Coach not found with id: $coachId") }
        
        return athleteWorkoutRepository.findByCoachOrderByWorkoutDateDesc(coach)
            .map { convertToDto(it, it.athleteWorkoutExercises) }
    }

    fun getWorkoutsByAthleteInDateRange(
        athleteId: Long, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): List<AthleteWorkoutDto> {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        
        return athleteWorkoutRepository.findByAthleteAndDateRange(athlete, startDate, endDate)
            .map { convertToDto(it, it.athleteWorkoutExercises) }
    }



    /**
     * Calculate adherence percentage based on planned vs actual performance
     */
    private fun calculateAdherencePercentage(athleteWorkoutExercises: List<AthleteWorkoutExercise>): Double {
        if (athleteWorkoutExercises.isEmpty()) return 0.0
        
        val adherenceScores = athleteWorkoutExercises.mapNotNull { we ->
            when {
                we.sets == null && we.reps == null -> 0.0 // Skipped
                we.plannedSets == null || we.plannedReps == null -> 1.0 // No plan to compare against
                else -> {
                    val setAdherence = if (we.plannedSets!! > 0) (we.sets ?: 0).toDouble() / we.plannedSets!! else 1.0
                    val repAdherence = if (we.plannedReps!! > 0) (we.reps ?: 0).toDouble() / we.plannedReps!! else 1.0
                    // Cap adherence at 100% (don't penalize for exceeding plan)
                    minOf(1.0, (setAdherence + repAdherence) / 2.0)
                }
            }
        }
        
        return adherenceScores.average() * 100.0
    }

    /**
     * Convert Workout entity to DTO with workout exercises
     */
    private fun convertToDto(athleteWorkout: AthleteWorkout, athleteWorkoutExercises: List<AthleteWorkoutExercise>): AthleteWorkoutDto {
        return AthleteWorkoutDto(
            id = athleteWorkout.id,
            athlete = AthleteBasicDto(
                id = athleteWorkout.athlete.id,
                firstName = athleteWorkout.athlete.firstName,
                lastName = athleteWorkout.athlete.lastName,
                dateOfBirth = athleteWorkout.athlete.dateOfBirth,
                sport = athleteWorkout.athlete.sport.name
            ),
            coach = UserBasicDto(
                id = athleteWorkout.coach.id,
                firstName = athleteWorkout.coach.firstName,
                lastName = athleteWorkout.coach.lastName,
                email = athleteWorkout.coach.email
            ),
            programWorkout = athleteWorkout.programWorkout?.let { pw ->
                ProgramWorkoutBasicDto(
                    id = pw.id,
                    name = pw.name,
                    description = pw.description,
                    workoutType = pw.workoutType.name,
                    estimatedDuration = pw.estimatedDuration,
                    orderInProgram = pw.orderInProgram,
                    exerciseCount = athleteWorkoutExercises.size
                )
            },
            workoutDate = athleteWorkout.workoutDate,
            name = athleteWorkout.name,
            notes = athleteWorkout.notes,
            rpe = athleteWorkout.rpe,
            duration = athleteWorkout.duration,
            createdAt = athleteWorkout.createdAt,
            workoutExercises = athleteWorkoutExercises.map { convertToDto(it) },
            summary = AthleteWorkoutSummaryDto(
                totalExercises = athleteWorkoutExercises.size,
                completedExercises = athleteWorkoutExercises.count { it.sets != null && it.reps != null },
                skippedExercises = athleteWorkoutExercises.count { it.sets == null && it.reps == null },
                modifiedExercises = athleteWorkoutExercises.count { we ->
                    (we.plannedSets != null && we.sets != we.plannedSets) ||
                    (we.plannedReps != null && we.reps != we.plannedReps)
                },
                averageRpe = athleteWorkoutExercises.mapNotNull { it.rpe?.toDouble() }.takeIf { it.isNotEmpty() }?.average(),
                adherencePercentage = calculateAdherencePercentage(athleteWorkoutExercises),
                workoutType = athleteWorkout.programWorkout?.workoutType?.name,
                estimatedDuration = athleteWorkout.programWorkout?.estimatedDuration,
                actualDuration = athleteWorkout.duration
            )
        )
    }

    /**
     * Convert WorkoutExercise entity to DTO
     */
    private fun convertToDto(athleteWorkoutExercise: AthleteWorkoutExercise): AthleteWorkoutExerciseDto {
        return AthleteWorkoutExerciseDto(
            id = athleteWorkoutExercise.id,
            exercise = ExerciseBasicDto(
                id = athleteWorkoutExercise.exercise.id,
                name = athleteWorkoutExercise.exercise.name,
                category = athleteWorkoutExercise.exercise.category.name,
                muscleGroups = athleteWorkoutExercise.exercise.muscleGroup.name
            ),
            programWorkoutExercise = athleteWorkoutExercise.programWorkoutExercise?.let { pwe ->
                ProgramWorkoutExerciseDto(
                    id = pwe.id,
                    exercise = ExerciseBasicDto(
                        id = pwe.exercise.id,
                        name = pwe.exercise.name,
                        category = pwe.exercise.category.name,
                        muscleGroups = pwe.exercise.muscleGroup.name
                    ),
                    sets = pwe.sets,
                    reps = pwe.reps,
                    intensityPercentage = pwe.intensityPercentage,
                    weight = pwe.weight,
                    distance = pwe.distance,
                    time = pwe.time,
                    restTime = pwe.restTime,
                    orderInWorkout = pwe.orderInWorkout,
                    notes = pwe.notes,
                    coachInstructions = pwe.coachInstructions,
                    progressionType = pwe.progressionType?.name,
                    progressionValue = pwe.progressionValue,
                    isSuperset = pwe.isSuperset,
                    supersetGroup = pwe.supersetGroup,
                    isDropset = pwe.isDropset,
                    isFailure = pwe.isFailure
                )
            },
            planned = PlannedExerciseDto(
                sets = athleteWorkoutExercise.plannedSets,
                reps = athleteWorkoutExercise.plannedReps,
                weight = athleteWorkoutExercise.plannedWeight,
                distance = athleteWorkoutExercise.plannedDistance,
                time = athleteWorkoutExercise.plannedTime,
                restTime = athleteWorkoutExercise.plannedRestTime,
                intensity = athleteWorkoutExercise.plannedIntensity
            ),
            actual = ActualExerciseDto(
                sets = athleteWorkoutExercise.actualSets,
                reps = athleteWorkoutExercise.actualReps,
                weight = athleteWorkoutExercise.actualWeight,
                distance = athleteWorkoutExercise.actualDistance,
                time = athleteWorkoutExercise.actualTime,
                restTime = athleteWorkoutExercise.actualRestTime,
                intensity = athleteWorkoutExercise.actualIntensity
            ),
            notes = athleteWorkoutExercise.notes,
            orderInWorkout = athleteWorkoutExercise.orderInWorkout,
            completionStatus = athleteWorkoutExercise.completionStatus.name,
            exerciseRpe = athleteWorkoutExercise.rpe,
            isFromProgram = athleteWorkoutExercise.isFromProgram,
            // Legacy fields for backward compatibility
            sets = athleteWorkoutExercise.sets,
            reps = athleteWorkoutExercise.reps,
            weight = athleteWorkoutExercise.weight,
            distance = athleteWorkoutExercise.distance,
            time = athleteWorkoutExercise.time,
            restTime = athleteWorkoutExercise.restTime
        )
    }

    /**
     * Calculate intensity based on workout performance
     */
    private fun calculateIntensity(athleteWorkoutExercise: AthleteWorkoutExercise): Double? {
        return athleteWorkoutExercise.weight?.let { weight ->
            // For now, return the weight as intensity
            // In a real system, this would calculate % of 1RM
            weight
        }
    }


    fun updateWorkout(id: Long, request: UpdateWorkoutRequest): AthleteWorkoutDto {
        val existingWorkout = athleteWorkoutRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $id") }

        val updatedWorkout = existingWorkout.copy(
            name = request.name ?: existingWorkout.name,
            notes = request.notes ?: existingWorkout.notes,
            rpe = request.rpe ?: existingWorkout.rpe,
            duration = request.duration ?: existingWorkout.duration
        )

        val savedWorkout = athleteWorkoutRepository.save(updatedWorkout)
        return convertToDto(savedWorkout, savedWorkout.athleteWorkoutExercises)
    }

    fun deleteWorkout(id: Long) {
        if (!athleteWorkoutRepository.existsById(id)) {
            throw IllegalArgumentException("Workout not found with id: $id")
        }
        athleteWorkoutRepository.deleteById(id)
    }

    fun removeExerciseFromWorkout(workoutId: Long, exerciseId: Long) {
        // Verify workout exists
        if (!athleteWorkoutRepository.existsById(workoutId)) {
            throw IllegalArgumentException("Workout not found with id: $workoutId")
        }

        // Find and delete the workout exercise
        val workoutExercise = athleteWorkoutExerciseRepository.findByAthleteWorkoutIdAndExerciseId(workoutId, exerciseId)
            ?: throw IllegalArgumentException("Exercise with id $exerciseId not found in workout $workoutId")

        athleteWorkoutExerciseRepository.delete(workoutExercise)
    }

    fun getWorkoutsThisWeek(athleteId: Long): List<AthleteWorkout> {
        val now = LocalDateTime.now()
        val startOfWeek = now.with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0)
        val endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59)
        
        return athleteWorkoutRepository.findByAthleteIdAndWorkoutDateBetween(
            athleteId, 
            startOfWeek.toLocalDate(),
            endOfWeek.toLocalDate()
        )
    }

    fun getWorkoutStats(athleteId: Long): WorkoutStatsResponse {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        
        val totalWorkouts = athleteWorkoutRepository.countByAthlete(athlete)
        val workouts = athleteWorkoutRepository.findByAthleteOrderByWorkoutDateDesc(athlete)
        
        val averageRpe = workouts.mapNotNull { it.rpe }.average()
        val averageDuration = workouts.mapNotNull { it.duration }.average()
        val totalVolume = calculateTotalVolume(workouts)
        
        return WorkoutStatsResponse(
            totalWorkouts = totalWorkouts.toInt(),
            averageRpe = if (averageRpe.isNaN()) null else averageRpe,
            averageDuration = if (averageDuration.isNaN()) null else averageDuration.toInt(),
            totalVolume = totalVolume
        )
    }

    private fun calculateTotalVolume(athleteWorkouts: List<AthleteWorkout>): Double {
        return athleteWorkouts.sumOf { workout ->
            workout.athleteWorkoutExercises.sumOf { exercise ->
                val sets = exercise.sets ?: 0
                val reps = exercise.reps ?: 0
                val weight = exercise.weight ?: 0.0
                sets * reps * weight
            }
        }
    }

    // Workout Completion Methods
    fun updateWorkoutExercise(
        workoutExerciseId: Long,
        request: ExerciseCompletionRequest
    ): AthleteWorkoutExerciseDto {
        val workoutExercise = athleteWorkoutExerciseRepository.findById(workoutExerciseId)
            .orElseThrow { IllegalArgumentException("Workout exercise not found with id: $workoutExerciseId") }

        // Detect if this is a personal record before updating
        val prDetectionResult = personalRecordService.detectWorkoutPRWithOneRepMax(workoutExercise.copy(
            actualSets = request.actualSets,
            actualReps = request.actualReps,
            actualWeight = request.actualWeight,
            actualDistance = request.actualDistance,
            actualTime = request.actualTime,
            actualRestTime = request.actualRestTime
        ))

        val updatedExercise = workoutExercise.copy(
            actualSets = request.actualSets,
            actualReps = request.actualReps,
            actualWeight = request.actualWeight,
            actualDistance = request.actualDistance,
            actualTime = request.actualTime,
            actualRestTime = request.actualRestTime,
            notes = request.notes,
            isPR = prDetectionResult.isPR,
            completionStatus = when {
                request.actualSets != null && request.actualReps != null -> ExerciseCompletionStatus.COMPLETED
                request.actualSets == null && request.actualReps == null -> ExerciseCompletionStatus.SKIPPED
                else -> ExerciseCompletionStatus.MODIFIED
            }
        )

        val savedExercise = athleteWorkoutExerciseRepository.save(updatedExercise)

        // Create performance metric if this is a PR
        if (prDetectionResult.isPR && prDetectionResult.metricType != null && prDetectionResult.newValue != null) {
            try {
                performanceIntegrationService.createMetricFromWorkoutPR(
                    savedExercise,
                    prDetectionResult.metricType!!,
                    prDetectionResult.newValue!!,
                    prDetectionResult.unit ?: "lbs"
                )
            } catch (e: Exception) {
                // Log the error but don't fail the workout exercise update
                println("Failed to create performance metric from workout PR: ${e.message}")
            }
        }

        return convertToDto(savedExercise)
    }

    fun startWorkout(workoutId: Long): AthleteWorkoutDto {
        val workout = athleteWorkoutRepository.findById(workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $workoutId") }

        // Mark all exercises as in progress
        val updatedExercises = workout.athleteWorkoutExercises.map { exercise ->
            exercise.copy(completionStatus = ExerciseCompletionStatus.IN_PROGRESS)
        }
        athleteWorkoutExerciseRepository.saveAll(updatedExercises)

        return convertToDto(workout, updatedExercises)
    }

    fun completeWorkout(request: CompleteWorkoutRequest): AthleteWorkoutDto {
        val workout = athleteWorkoutRepository.findById(request.workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: ${request.workoutId}") }

        // Update workout with completion info
        val updatedWorkout = workout.copy(
            rpe = request.rpe,
            duration = request.duration,
            notes = request.notes
        )
        athleteWorkoutRepository.save(updatedWorkout)

        // Update exercise completions
        request.exerciseCompletions.forEach { completion ->
            updateWorkoutExercise(completion.workoutExerciseId, completion)
        }

        // Get updated workout with all exercises
        val updatedWorkoutWithExercises = athleteWorkoutRepository.findById(request.workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found") }

        return convertToDto(updatedWorkoutWithExercises, updatedWorkoutWithExercises.athleteWorkoutExercises)
    }
}

// Request DTOs
data class CreateAthleteWorkoutRequest(
    val athleteId: Long,
    val coachId: Long,
    val workoutDate: LocalDateTime,
    val name: String? = null,
    val notes: String? = null,
    val rpe: Int? = null,
    val duration: Int? = null,
    val exercises: List<CreateWorkoutExerciseRequest> = emptyList()
)

data class CreateWorkoutExerciseRequest(
    val exerciseId: Long,
    val programWorkoutExerciseId: Long? = null, // Optional link to program workout exercise
    val sets: Int? = null,
    val reps: Int? = null,
    val weight: Double? = null,
    val distance: Double? = null,
    val time: Int? = null,
    val restTime: Int? = null,
    val notes: String? = null,
    val plannedSets: Int? = null, // From program workout exercise
    val plannedReps: Int? = null, // From program workout exercise
    val plannedIntensity: Double? = null, // From program workout exercise
    val isFromProgram: Boolean = false // Whether this exercise came from a program
)

data class UpdateWorkoutRequest(
    val name: String? = null,
    val notes: String? = null,
    val rpe: Int? = null,
    val duration: Int? = null
)



data class CompleteWorkoutRequest(
    val workoutId: Long,
    val completedById: Long,
    val completedDate: java.time.LocalDate,
    val rpe: Int? = null,
    val duration: Int? = null,
    val notes: String? = null,
    val exerciseCompletions: List<ExerciseCompletionRequest>
)

data class ExerciseCompletionRequest(
    val workoutExerciseId: Long,
    val actualSets: Int? = null,
    val actualReps: Int? = null,
    val actualWeight: Double? = null,
    val actualDistance: Double? = null,
    val actualTime: Int? = null,
    val actualRestTime: Int? = null,
    val notes: String? = null
)

// Response DTOs
data class WorkoutStatsResponse(
    val totalWorkouts: Int,
    val averageRpe: Double?,
    val averageDuration: Int?,
    val totalVolume: Double
)