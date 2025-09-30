package com.athletetracker.service

import com.athletetracker.dto.AthleteBasicDto
import com.athletetracker.dto.ExerciseBasicDto
import com.athletetracker.dto.UserBasicDto
import com.athletetracker.dto.WorkoutDto
import com.athletetracker.dto.WorkoutExerciseDto
import com.athletetracker.dto.WorkoutSummaryDto
import com.athletetracker.dto.ProgramWorkoutBasicDto
import com.athletetracker.dto.ProgramWorkoutExerciseDto
import com.athletetracker.dto.PlannedExerciseDto
import com.athletetracker.dto.ActualExerciseDto
import com.athletetracker.entity.Athlete
import com.athletetracker.entity.CompletionStatus
import com.athletetracker.entity.ExerciseCompletionStatus
import com.athletetracker.entity.ProgramProgress
import com.athletetracker.entity.User
import com.athletetracker.entity.Workout
import com.athletetracker.entity.WorkoutExercise
import com.athletetracker.repository.AthleteRepository
import com.athletetracker.repository.AthleteProgramRepository
import com.athletetracker.repository.ExerciseRepository
import com.athletetracker.repository.ProgramProgressRepository
import com.athletetracker.repository.UserRepository
import com.athletetracker.repository.WorkoutRepository
import com.athletetracker.repository.WorkoutExerciseRepository
import com.athletetracker.repository.ProgramWorkoutExerciseRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Transactional
class WorkoutService(
    private val workoutRepository: WorkoutRepository,
    private val athleteRepository: AthleteRepository,
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository,
    private val workoutExerciseRepository: WorkoutExerciseRepository,
    private val programWorkoutExerciseRepository: ProgramWorkoutExerciseRepository,
    private val personalRecordService: PersonalRecordService,
    private val performanceIntegrationService: PerformanceIntegrationService
) {

    fun createWorkout(request: CreateWorkoutRequest): Any {
        val athlete = athleteRepository.findById(request.athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: ${request.athleteId}") }
        
        val coach = userRepository.findById(request.coachId)
            .orElseThrow { IllegalArgumentException("Coach not found with id: ${request.coachId}") }

        val workout = Workout(
            athlete = athlete,
            coach = coach,
            workoutDate = request.workoutDate,
            name = request.name,
            notes = request.notes,
            rpe = request.rpe,
            duration = request.duration
        )

        val savedWorkout = workoutRepository.save(workout)

        // Handle workout exercises if provided
        if (request.exercises.isNotEmpty()) {
            val workoutExercises = request.exercises.mapIndexed { index, exerciseRequest ->
                val exercise = exerciseRepository.findById(exerciseRequest.exerciseId)
                    .orElseThrow { IllegalArgumentException("Exercise not found with id: ${exerciseRequest.exerciseId}") }

                val programWorkoutExercise = exerciseRequest.programWorkoutExerciseId?.let { programWorkoutExerciseId ->
                    programWorkoutExerciseRepository.findById(programWorkoutExerciseId)
                        .orElseThrow { IllegalArgumentException("Program workout exercise not found with id: $programWorkoutExerciseId") }
                }

                WorkoutExercise(
                    workout = savedWorkout,
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
            workoutExerciseRepository.saveAll(workoutExercises)
            return convertToDto(savedWorkout, workoutExercises)
        }

        return savedWorkout
    }

    fun getWorkoutById(id: Long): WorkoutDto {
        return workoutRepository.findById(id)
            .map { convertToDto(it, it.workoutExercises) }
            .orElseThrow { IllegalArgumentException("Workout not found with id: $id") }
    }

    fun getWorkoutsByAthlete(athleteId: Long): List<Workout> {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        
        return workoutRepository.findByAthleteOrderByWorkoutDateDesc(athlete)
    }

    fun getWorkoutsByAthleteAsDto(athleteId: Long): List<WorkoutDto> {
        val workouts = getWorkoutsByAthlete(athleteId)
        return workouts.map { convertToDto(it, it.workoutExercises) }
    }

    fun getWorkoutsByCoach(coachId: Long): List<Workout> {
        val coach = userRepository.findById(coachId)
            .orElseThrow { IllegalArgumentException("Coach not found with id: $coachId") }
        
        return workoutRepository.findByCoachOrderByWorkoutDateDesc(coach)
    }

    fun getWorkoutsByAthleteInDateRange(
        athleteId: Long, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): List<Workout> {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        
        return workoutRepository.findByAthleteAndDateRange(athlete, startDate, endDate)
    }



    /**
     * Calculate adherence percentage based on planned vs actual performance
     */
    private fun calculateAdherencePercentage(workoutExercises: List<WorkoutExercise>): Double {
        if (workoutExercises.isEmpty()) return 0.0
        
        val adherenceScores = workoutExercises.mapNotNull { we ->
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
    private fun convertToDto(workout: Workout, workoutExercises: List<WorkoutExercise>): WorkoutDto {
        return WorkoutDto(
            id = workout.id,
            athlete = AthleteBasicDto(
                id = workout.athlete.id,
                firstName = workout.athlete.firstName,
                lastName = workout.athlete.lastName,
                dateOfBirth = workout.athlete.dateOfBirth,
                sport = workout.athlete.sport.name
            ),
            coach = UserBasicDto(
                id = workout.coach.id,
                firstName = workout.coach.firstName,
                lastName = workout.coach.lastName,
                email = workout.coach.email
            ),
            programWorkout = workout.programWorkout?.let { pw ->
                ProgramWorkoutBasicDto(
                    id = pw.id,
                    name = pw.name,
                    description = pw.description,
                    workoutType = pw.workoutType.name,
                    estimatedDuration = pw.estimatedDuration,
                    orderInProgram = pw.orderInProgram,
                    exerciseCount = workoutExercises.size
                )
            },
            workoutDate = workout.workoutDate,
            name = workout.name,
            notes = workout.notes,
            rpe = workout.rpe,
            duration = workout.duration,
            createdAt = workout.createdAt,
            workoutExercises = workoutExercises.map { convertToDto(it) },
            summary = WorkoutSummaryDto(
                totalExercises = workoutExercises.size,
                completedExercises = workoutExercises.count { it.sets != null && it.reps != null },
                skippedExercises = workoutExercises.count { it.sets == null && it.reps == null },
                modifiedExercises = workoutExercises.count { we ->
                    (we.plannedSets != null && we.sets != we.plannedSets) ||
                    (we.plannedReps != null && we.reps != we.plannedReps)
                },
                averageRpe = workoutExercises.mapNotNull { it.rpe?.toDouble() }.takeIf { it.isNotEmpty() }?.average(),
                adherencePercentage = calculateAdherencePercentage(workoutExercises),
                workoutType = workout.programWorkout?.workoutType?.name,
                estimatedDuration = workout.programWorkout?.estimatedDuration,
                actualDuration = workout.duration
            )
        )
    }

    /**
     * Convert WorkoutExercise entity to DTO
     */
    private fun convertToDto(workoutExercise: WorkoutExercise): WorkoutExerciseDto {
        return WorkoutExerciseDto(
            id = workoutExercise.id,
            exercise = ExerciseBasicDto(
                id = workoutExercise.exercise.id,
                name = workoutExercise.exercise.name,
                category = workoutExercise.exercise.category.name,
                muscleGroups = workoutExercise.exercise.muscleGroup.name
            ),
            programWorkoutExercise = workoutExercise.programWorkoutExercise?.let { pwe ->
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
                sets = workoutExercise.plannedSets,
                reps = workoutExercise.plannedReps,
                weight = workoutExercise.plannedWeight,
                distance = workoutExercise.plannedDistance,
                time = workoutExercise.plannedTime,
                restTime = workoutExercise.plannedRestTime,
                intensity = workoutExercise.plannedIntensity
            ),
            actual = ActualExerciseDto(
                sets = workoutExercise.sets,
                reps = workoutExercise.reps,
                weight = workoutExercise.weight,
                distance = workoutExercise.distance,
                time = workoutExercise.time,
                restTime = workoutExercise.restTime,
                intensity = workoutExercise.actualIntensity
            ),
            notes = workoutExercise.notes,
            orderInWorkout = workoutExercise.orderInWorkout,
            completionStatus = workoutExercise.completionStatus.name,
            exerciseRpe = workoutExercise.rpe,
            isFromProgram = workoutExercise.isFromProgram,
            // Legacy fields for backward compatibility
            sets = workoutExercise.sets,
            reps = workoutExercise.reps,
            weight = workoutExercise.weight,
            distance = workoutExercise.distance,
            time = workoutExercise.time,
            restTime = workoutExercise.restTime
        )
    }

    /**
     * Calculate intensity based on workout performance
     */
    private fun calculateIntensity(workoutExercise: WorkoutExercise): Double? {
        return workoutExercise.weight?.let { weight ->
            // For now, return the weight as intensity
            // In a real system, this would calculate % of 1RM
            weight
        }
    }


    fun updateWorkout(id: Long, request: UpdateWorkoutRequest): Workout {
        val existingWorkout = workoutRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $id") }

        val updatedWorkout = existingWorkout.copy(
            name = request.name ?: existingWorkout.name,
            notes = request.notes ?: existingWorkout.notes,
            rpe = request.rpe ?: existingWorkout.rpe,
            duration = request.duration ?: existingWorkout.duration
        )

        return workoutRepository.save(updatedWorkout)
    }

    fun deleteWorkout(id: Long) {
        if (!workoutRepository.existsById(id)) {
            throw IllegalArgumentException("Workout not found with id: $id")
        }
        workoutRepository.deleteById(id)
    }

    fun removeExerciseFromWorkout(workoutId: Long, exerciseId: Long) {
        // Verify workout exists
        if (!workoutRepository.existsById(workoutId)) {
            throw IllegalArgumentException("Workout not found with id: $workoutId")
        }

        // Find and delete the workout exercise
        val workoutExercise = workoutExerciseRepository.findByWorkoutIdAndExerciseId(workoutId, exerciseId)
            ?: throw IllegalArgumentException("Exercise with id $exerciseId not found in workout $workoutId")

        workoutExerciseRepository.delete(workoutExercise)
    }

    fun getWorkoutsThisWeek(athleteId: Long): List<Workout> {
        val now = LocalDateTime.now()
        val startOfWeek = now.with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0)
        val endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59)
        
        return workoutRepository.findByAthleteIdAndWorkoutDateBetween(
            athleteId, 
            startOfWeek.toLocalDate(),
            endOfWeek.toLocalDate()
        )
    }

    fun getWorkoutStats(athleteId: Long): WorkoutStatsResponse {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        
        val totalWorkouts = workoutRepository.countByAthlete(athlete)
        val workouts = workoutRepository.findByAthleteOrderByWorkoutDateDesc(athlete)
        
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

    private fun calculateTotalVolume(workouts: List<Workout>): Double {
        return workouts.sumOf { workout ->
            workout.workoutExercises.sumOf { exercise ->
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
    ): WorkoutExerciseDto {
        val workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
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

        val savedExercise = workoutExerciseRepository.save(updatedExercise)

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

    fun startWorkout(workoutId: Long): WorkoutDto {
        val workout = workoutRepository.findById(workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $workoutId") }

        // Mark all exercises as in progress
        val updatedExercises = workout.workoutExercises.map { exercise ->
            exercise.copy(completionStatus = ExerciseCompletionStatus.IN_PROGRESS)
        }
        workoutExerciseRepository.saveAll(updatedExercises)

        return convertToDto(workout, updatedExercises)
    }

    fun completeWorkout(request: CompleteWorkoutRequest): WorkoutDto {
        val workout = workoutRepository.findById(request.workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: ${request.workoutId}") }

        // Update workout with completion info
        val updatedWorkout = workout.copy(
            rpe = request.rpe,
            duration = request.duration,
            notes = request.notes
        )
        workoutRepository.save(updatedWorkout)

        // Update exercise completions
        request.exerciseCompletions.forEach { completion ->
            updateWorkoutExercise(completion.workoutExerciseId, completion)
        }

        // Get updated workout with all exercises
        val updatedWorkoutWithExercises = workoutRepository.findById(request.workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found") }

        return convertToDto(updatedWorkoutWithExercises, updatedWorkoutWithExercises.workoutExercises)
    }
}

// Request DTOs
data class CreateWorkoutRequest(
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