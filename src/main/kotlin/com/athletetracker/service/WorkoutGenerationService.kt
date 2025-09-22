package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.*
import com.athletetracker.repository.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class WorkoutGenerationService(
    private val athleteProgramRepository: AthleteProgramRepository,
    private val workoutRepository: WorkoutRepository,
    private val workoutExerciseRepository: WorkoutExerciseRepository,
    private val programWorkoutRepository: ProgramWorkoutRepository,
    private val programWorkoutExerciseRepository: ProgramWorkoutExerciseRepository
) {

    fun generateWorkoutsFromProgram(
        athleteProgramId: Long,
        startDate: LocalDate,
        numberOfWeeks: Int
    ): List<WorkoutDto> {
        val athleteProgram = athleteProgramRepository.findById(athleteProgramId)
            .orElseThrow { IllegalArgumentException("Athlete program not found with id: $athleteProgramId") }

        // Get all workout templates from the program
        val programWorkouts = programWorkoutRepository.findByProgramWithExercises(athleteProgram.program)
            .filter { it.isActive }
            .sortedBy { it.orderInProgram }

        if (programWorkouts.isEmpty()) {
            throw IllegalArgumentException("No active workout templates found in program: ${athleteProgram.program.name}")
        }

        val generatedWorkouts = mutableListOf<Workout>()

        // Generate workouts for specified number of weeks
        for (week in 0 until numberOfWeeks) {
            val weekStartDate = startDate.plusWeeks(week.toLong())
            
            // Generate each workout template for this week
            programWorkouts.forEachIndexed { dayIndex, programWorkout ->
                val workoutDate = weekStartDate.plusDays(dayIndex.toLong())
                
                // Check if workout already exists for this date
                val existingWorkout = workoutRepository.findByAthleteAndWorkoutDate(
                    athleteProgram.athlete, 
                    workoutDate.atStartOfDay()
                )
                
                if (existingWorkout == null) {
                    val workout = createWorkoutFromTemplate(
                        programWorkout,
                        athleteProgram.athlete,
                        athleteProgram.assignedBy,
                        workoutDate
                    )
                    generatedWorkouts.add(workout)
                }
            }
        }

        // Save all generated workouts
        val savedWorkouts = workoutRepository.saveAll(generatedWorkouts)
        
        // Convert to DTOs and return
        return savedWorkouts.map { convertToWorkoutDto(it) }
    }

    private fun createWorkoutFromTemplate(
        programWorkout: ProgramWorkout,
        athlete: Athlete,
        coach: User,
        workoutDate: LocalDate
    ): Workout {
        // Create the workout from template
        val workout = Workout(
            athlete = athlete,
            coach = coach,
            programWorkout = programWorkout,
            workoutDate = workoutDate.atTime(9, 0), // Default to 9 AM
            name = programWorkout.name,
            notes = programWorkout.notes
        )

        val savedWorkout = workoutRepository.save(workout)

        // Create workout exercises from template
        val workoutExercises = programWorkout.exercises
            .sortedBy { it.orderInWorkout }
            .map { programWorkoutExercise ->
                WorkoutExercise(
                    workout = savedWorkout,
                    exercise = programWorkoutExercise.exercise,
                    programWorkoutExercise = programWorkoutExercise,
                    plannedSets = programWorkoutExercise.sets,
                    plannedReps = programWorkoutExercise.reps,
                    plannedWeight = programWorkoutExercise.weight,
                    plannedDistance = programWorkoutExercise.distance,
                    plannedTime = programWorkoutExercise.time,
                    plannedRestTime = programWorkoutExercise.restTime,
                    plannedIntensity = programWorkoutExercise.intensityPercentage,
                    notes = programWorkoutExercise.notes,
                    orderInWorkout = programWorkoutExercise.orderInWorkout,
                    isFromProgram = true,
                    completionStatus = ExerciseCompletionStatus.PLANNED
                )
            }

        workoutExerciseRepository.saveAll(workoutExercises)

        return savedWorkout
    }

    fun getGeneratedWorkouts(athleteProgramId: Long): List<WorkoutDto> {
        val athleteProgram = athleteProgramRepository.findById(athleteProgramId)
            .orElseThrow { IllegalArgumentException("Athlete program not found with id: $athleteProgramId") }

        val workouts = workoutRepository.findByAthleteAndProgramWorkoutNotNull(athleteProgram.athlete)
            .filter { it.programWorkout?.program?.id == athleteProgram.program.id }
            .sortedBy { it.workoutDate }

        return workouts.map { convertToWorkoutDto(it) }
    }

    fun regenerateWorkoutFromTemplate(workoutId: Long): WorkoutDto {
        val existingWorkout = workoutRepository.findById(workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $workoutId") }

        val programWorkout = existingWorkout.programWorkout
            ?: throw IllegalArgumentException("Workout is not linked to a program template")

        // Clear existing exercises
        workoutExerciseRepository.deleteByWorkout(existingWorkout)

        // Recreate exercises from template
        val newExercises = programWorkout.exercises
            .sortedBy { it.orderInWorkout }
            .map { programWorkoutExercise ->
                WorkoutExercise(
                    workout = existingWorkout,
                    exercise = programWorkoutExercise.exercise,
                    programWorkoutExercise = programWorkoutExercise,
                    plannedSets = programWorkoutExercise.sets,
                    plannedReps = programWorkoutExercise.reps,
                    plannedWeight = programWorkoutExercise.weight,
                    plannedDistance = programWorkoutExercise.distance,
                    plannedTime = programWorkoutExercise.time,
                    plannedRestTime = programWorkoutExercise.restTime,
                    plannedIntensity = programWorkoutExercise.intensityPercentage,
                    notes = programWorkoutExercise.notes,
                    orderInWorkout = programWorkoutExercise.orderInWorkout,
                    isFromProgram = true,
                    completionStatus = ExerciseCompletionStatus.PLANNED
                )
            }

        workoutExerciseRepository.saveAll(newExercises)

        return convertToWorkoutDto(existingWorkout)
    }

    private fun convertToWorkoutDto(workout: Workout): WorkoutDto {
        return WorkoutDto(
            id = workout.id,
            athlete = AthleteBasicDto(
                id = workout.athlete.id,
                firstName = workout.athlete.firstName,
                lastName = workout.athlete.lastName,
                sport = workout.athlete.sport.name,
                dateOfBirth = workout.athlete.dateOfBirth
            ),
            coach = UserBasicDto(
                id = workout.coach.id,
                firstName = workout.coach.firstName,
                lastName = workout.coach.lastName,
                email = workout.coach.email
            ),
            programWorkout = workout.programWorkout?.let {
                ProgramWorkoutBasicDto(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    workoutType = it.workoutType.name,
                    estimatedDuration = it.estimatedDuration,
                    orderInProgram = it.orderInProgram,
                    exerciseCount = it.exercises.size
                )
            },
            workoutDate = workout.workoutDate,
            name = workout.name,
            notes = workout.notes,
            rpe = workout.rpe,
            duration = workout.duration,
            createdAt = workout.createdAt,
            workoutExercises = workout.workoutExercises.map { exercise ->
                WorkoutExerciseDto(
                    id = exercise.id,
                    exercise = ExerciseBasicDto(
                        id = exercise.exercise.id,
                        name = exercise.exercise.name,
                        category = exercise.exercise.category.name,
                        muscleGroups = exercise.exercise.muscleGroup.name
                    ),
                    planned = PlannedExerciseDto(
                        sets = exercise.plannedSets,
                        reps = exercise.plannedReps,
                        weight = exercise.plannedWeight,
                        distance = exercise.plannedDistance,
                        time = exercise.plannedTime,
                        restTime = exercise.plannedRestTime,
                        intensity = exercise.plannedIntensity
                    ),
                    actual = ActualExerciseDto(
                        sets = exercise.actualSets,
                        reps = exercise.actualReps,
                        weight = exercise.actualWeight,
                        distance = exercise.actualDistance,
                        time = exercise.actualTime,
                        restTime = exercise.actualRestTime,
                        intensity = exercise.actualIntensity
                    ),
                    orderInWorkout = exercise.orderInWorkout,
                    notes = exercise.notes,
                    completionStatus = exercise.completionStatus.name,
                    isFromProgram = exercise.isFromProgram,
                    programWorkoutExercise = exercise.programWorkoutExercise?.let { pwe ->
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
                    exerciseRpe = exercise.rpe,
                    sets = exercise.actualSets ?: exercise.plannedSets,
                    reps = exercise.actualReps ?: exercise.plannedReps,
                    weight = exercise.actualWeight ?: exercise.plannedWeight,
                    distance = exercise.actualDistance ?: exercise.plannedDistance,
                    time = exercise.actualTime ?: exercise.plannedTime,
                    restTime = exercise.actualRestTime ?: exercise.plannedRestTime
                )
            },
            summary = WorkoutSummaryDto(
                totalExercises = workout.workoutExercises.size,
                completedExercises = workout.workoutExercises.count { it.sets != null && it.reps != null },
                skippedExercises = workout.workoutExercises.count { it.sets == null && it.reps == null },
                modifiedExercises = workout.workoutExercises.count { we ->
                    we.actualSets != null && we.actualReps != null && we.actualWeight != null && we.actualDistance != null && we.actualTime != null && we.actualRestTime != null && we.actualIntensity != null
                },
                averageRpe = workout.workoutExercises
                    .mapNotNull { it.rpe?.toDouble() }
                    .takeIf { it.isNotEmpty() }
                    ?.average(),
                adherencePercentage = if (workout.workoutExercises.isNotEmpty()) {
                    (workout.workoutExercises.count { it.completionStatus == ExerciseCompletionStatus.COMPLETED }.toDouble() / 
                     workout.workoutExercises.size * 100)
                } else 0.0,
                workoutType = workout.programWorkout?.workoutType?.name,
                estimatedDuration = workout.programWorkout?.estimatedDuration,
                actualDuration = workout.duration
            )
        )
    }
}

