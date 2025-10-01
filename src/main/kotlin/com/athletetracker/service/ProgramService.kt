package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.*
import com.athletetracker.repository.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Transactional
class ProgramService(
    private val programRepository: ProgramRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userRepository: UserRepository,
    private val programWorkoutRepository: ProgramWorkoutRepository,
    private val programWorkoutExerciseRepository: ProgramWorkoutExerciseRepository
) {

    fun createBasicProgram(request: CreateBasicProgramRequest): ProgramDto {
        val createdBy = userRepository.findById(request.createdById)
            .orElseThrow { IllegalArgumentException("User not found with id: ${request.createdById}") }

        val program = Program(
            name = request.name,
            description = request.description,
            sport = request.sport,
            durationWeeks = request.durationWeeks,
            difficultyLevel = request.difficultyLevel,
            goals = request.goals,
            createdBy = createdBy,
            isTemplate = request.isTemplate ?: false
        )

        val savedProgram = programRepository.save(program)
        return convertToDto(savedProgram)
    }

    fun getProgramById(id: Long): ProgramDetailResponse {
        val program = programRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Program not found with id: $id") }
        return convertToProgramDetailResponse(program)
    }
    
    private fun convertToProgramDetailResponse(program: Program): ProgramDetailResponse {
        val workouts = programWorkoutRepository.findByProgramWithExercises(program)
            .filter { it.isActive }
        val workoutDtos = workouts.map { convertToProgramWorkoutDto(it) }
        
        return ProgramDetailResponse(
            id = program.id,
            name = program.name,
            description = program.description,
            sport = program.sport.name,
            durationWeeks = program.durationWeeks,
            difficultyLevel = program.difficultyLevel,
            goals = program.goals,
            createdBy = UserBasicDto(
                id = program.createdBy.id,
                firstName = program.createdBy.firstName,
                lastName = program.createdBy.lastName,
                email = program.createdBy.email
            ),
            isActive = program.isActive,
            isTemplate = program.isTemplate,
            createdAt = program.createdAt.toString(),
            updatedAt = program.updatedAt.toString(),
            workouts = workoutDtos,
            summary = calculateProgramSummary(workoutDtos)
        )
    }
    
    private fun convertToProgramWorkoutDto(programWorkout: ProgramWorkout): ProgramWorkoutDto {
        val exercises = programWorkout.exercises.map { convertToProgramWorkoutExerciseDto(it) }
        
        return ProgramWorkoutDto(
            id = programWorkout.id,
            name = programWorkout.name,
            description = programWorkout.description,
            workoutType = programWorkout.workoutType.name,
            estimatedDuration = programWorkout.estimatedDuration,
            orderInProgram = programWorkout.orderInProgram,
            notes = programWorkout.notes,
            warmupInstructions = programWorkout.warmupInstructions,
            cooldownInstructions = programWorkout.cooldownInstructions,
            isActive = programWorkout.isActive,
            createdAt = programWorkout.createdAt,
            updatedAt = programWorkout.updatedAt,
            exercises = exercises
        )
    }
    
    private fun convertToProgramWorkoutExerciseDto(exercise: ProgramWorkoutExercise): ProgramWorkoutExerciseDto {
        return ProgramWorkoutExerciseDto(
            id = exercise.id,
            exercise = ExerciseBasicDto(
                id = exercise.exercise.id,
                name = exercise.exercise.name,
                category = exercise.exercise.category.name,
                muscleGroups = exercise.exercise.muscleGroup.name
            ),
            sets = exercise.sets,
            reps = exercise.reps,
            intensityPercentage = exercise.intensityPercentage,
            weight = exercise.weight,
            distance = exercise.distance,
            time = exercise.time,
            restTime = exercise.restTime,
            orderInWorkout = exercise.orderInWorkout,
            notes = exercise.notes,
            coachInstructions = exercise.coachInstructions,
            progressionType = exercise.progressionType?.name,
            progressionValue = exercise.progressionValue,
            isSuperset = exercise.isSuperset,
            supersetGroup = exercise.supersetGroup,
            isDropset = exercise.isDropset,
            isFailure = exercise.isFailure
        )
    }
    
    private fun calculateProgramSummary(workouts: List<ProgramWorkoutDto>): ProgramSummary {
        val totalWorkouts = workouts.size
        val totalExercises = workouts.sumOf { it.exercises.size }
        val estimatedWeeklyDuration = workouts.sumOf { it.estimatedDuration ?: 0 }
        val workoutTypeBreakdown = workouts.groupingBy { it.workoutType }.eachCount()
        
        return ProgramSummary(
            totalWorkouts = totalWorkouts,
            totalExercises = totalExercises,
            estimatedWeeklyDuration = estimatedWeeklyDuration.takeIf { it > 0 },
            workoutTypeBreakdown = workoutTypeBreakdown
        )
    }

    fun getAllActivePrograms(): List<ProgramDto> {
        return programRepository.findByIsActiveTrue().map { convertToDto(it) }
    }

    fun getProgramTemplates(): List<ProgramDto> {
        return programRepository.findByIsActiveTrueAndIsTemplateTrue().map { convertToDto(it) }
    }

    fun getProgramsBySport(sport: Sport): List<ProgramDto> {
        return programRepository.findByIsActiveTrueAndSport(sport).map { convertToDto(it) }
    }

    fun getProgramTemplatesBySport(sport: Sport): List<ProgramDto> {
        return programRepository.findByIsActiveTrueAndSportAndIsTemplateTrue(sport).map { convertToDto(it) }
    }

    fun getProgramsByCreator(creatorId: Long): List<ProgramDto> {
        val creator = userRepository.findById(creatorId)
            .orElseThrow { IllegalArgumentException("User not found with id: $creatorId") }
        
        return programRepository.findByCreatedByOrderByCreatedAtDesc(creator).map { convertToDto(it) }
    }

    fun searchPrograms(query: String): List<ProgramDto> {
        return programRepository.searchActivePrograms(query).map { convertToDto(it) }
    }

    fun updateProgram(id: Long, request: UpdateProgramRequest): ProgramDto {
        val existingProgram = programRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Program not found with id: $id") }

        val updatedProgram = existingProgram.copy(
            name = request.name ?: existingProgram.name,
            description = request.description ?: existingProgram.description,
            durationWeeks = request.durationWeeks ?: existingProgram.durationWeeks,
            difficultyLevel = request.difficultyLevel ?: existingProgram.difficultyLevel,
            goals = request.goals ?: existingProgram.goals,
            updatedAt = LocalDateTime.now()
        )

        val savedProgram = programRepository.save(updatedProgram)
        return convertToDto(savedProgram)
    }

    fun deleteProgram(id: Long) {
        val program = programRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Program not found with id: $id") }
        val deactivatedProgram = program.copy(isActive = false)
        programRepository.save(deactivatedProgram)
    }

    fun duplicateProgram(id: Long, request: DuplicateProgramRequest): ProgramDto {
        val originalProgram = programRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Program not found with id: $id") }
        val createdBy = userRepository.findById(request.createdById)
            .orElseThrow { IllegalArgumentException("User not found with id: ${request.createdById}") }

        // Create duplicate program (basic info only - workout templates handled separately)
        val duplicateProgram = Program(
            name = request.name ?: "${originalProgram.name} (Copy)",
            description = originalProgram.description,
            sport = originalProgram.sport,
            durationWeeks = originalProgram.durationWeeks,
            difficultyLevel = originalProgram.difficultyLevel,
            goals = originalProgram.goals,
            createdBy = createdBy,
            isTemplate = request.isTemplate ?: false
        )

        val savedProgram = programRepository.save(duplicateProgram)
        return convertToDto(savedProgram)
    }
    
    fun convertToDto(program: Program): ProgramDto {
        return ProgramDto(
            id = program.id,
            name = program.name,
            description = program.description,
            sport = program.sport,
            durationWeeks = program.durationWeeks,
            difficultyLevel = program.difficultyLevel,
            goals = program.goals,
            isActive = program.isActive,
            isTemplate = program.isTemplate,
            createdBy = UserBasicDto(
                id = program.createdBy.id,
                firstName = program.createdBy.firstName,
                lastName = program.createdBy.lastName,
                email = program.createdBy.email
            ),
            createdAt = program.createdAt.toString(),
            updatedAt = program.updatedAt.toString()
        )
    }

    // Workout Management Methods
    fun addWorkoutToProgram(programId: Long, request: CreateProgramWorkoutRequest): ProgramWorkoutDto {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }

        val workout = ProgramWorkout(
            program = program,
            name = request.name,
            description = request.description,
            workoutType = WorkoutType.valueOf(request.workoutType),
            estimatedDuration = request.estimatedDuration,
            orderInProgram = request.orderInProgram,
            notes = request.notes,
            warmupInstructions = request.warmupInstructions,
            cooldownInstructions = request.cooldownInstructions
        )

        val savedWorkout = programWorkoutRepository.save(workout)

        // Add exercises to the workout
        val exercises = request.exercises.map { exerciseRequest ->
            val exercise = exerciseRepository.findById(exerciseRequest.exerciseId)
                .orElseThrow { IllegalArgumentException("Exercise not found with id: ${exerciseRequest.exerciseId}") }

            ProgramWorkoutExercise(
                programWorkout = savedWorkout,
                exercise = exercise,
                sets = exerciseRequest.sets,
                reps = exerciseRequest.reps,
                intensityPercentage = exerciseRequest.intensityPercentage,
                weight = exerciseRequest.weight,
                distance = exerciseRequest.distance,
                time = exerciseRequest.time,
                restTime = exerciseRequest.restTime,
                orderInWorkout = exerciseRequest.orderInWorkout,
                notes = exerciseRequest.notes,
                coachInstructions = exerciseRequest.coachInstructions,
                progressionType = exerciseRequest.progressionType?.let { ProgressionType.valueOf(it) },
                progressionValue = exerciseRequest.progressionValue,
                isSuperset = exerciseRequest.isSuperset,
                supersetGroup = exerciseRequest.supersetGroup,
                isDropset = exerciseRequest.isDropset,
                isFailure = exerciseRequest.isFailure
            )
        }

        val savedExercises = programWorkoutExerciseRepository.saveAll(exercises)

        // Create a copy of the workout with the saved exercises for DTO conversion
        val workoutWithExercises = savedWorkout.copy(exercises = savedExercises.toList())
        
        return convertToProgramWorkoutDto(workoutWithExercises)
    }

    fun getProgramWorkouts(programId: Long): List<ProgramWorkoutDto> {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }
        
        val workouts = programWorkoutRepository.findByProgramWithExercises(program)
            .filter { it.isActive }
        return workouts.map { convertToProgramWorkoutDto(it) }
    }

    fun updateProgramWorkout(programId: Long, workoutId: Long, request: UpdateProgramWorkoutRequest): ProgramWorkoutDto {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }
        
        val existingWorkout = programWorkoutRepository.findById(workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $workoutId") }
        
        if (existingWorkout.program.id != programId) {
            throw IllegalArgumentException("Workout does not belong to the specified program")
        }

        val updatedWorkout = existingWorkout.copy(
            name = request.name ?: existingWorkout.name,
            description = request.description ?: existingWorkout.description,
            workoutType = request.workoutType?.let { WorkoutType.valueOf(it) } ?: existingWorkout.workoutType,
            estimatedDuration = request.estimatedDuration ?: existingWorkout.estimatedDuration,
            orderInProgram = request.orderInProgram ?: existingWorkout.orderInProgram,
            notes = request.notes ?: existingWorkout.notes,
            warmupInstructions = request.warmupInstructions ?: existingWorkout.warmupInstructions,
            cooldownInstructions = request.cooldownInstructions ?: existingWorkout.cooldownInstructions,
            updatedAt = LocalDateTime.now()
        )

        val savedWorkout = programWorkoutRepository.save(updatedWorkout)
        
        // Create a copy of the saved workout with the existing exercises for DTO conversion
        val workoutWithExercises = savedWorkout.copy(exercises = existingWorkout.exercises)
        
        return convertToProgramWorkoutDto(workoutWithExercises)
    }

    fun deleteProgramWorkout(programId: Long, workoutId: Long) {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }
        
        val workout = programWorkoutRepository.findById(workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $workoutId") }
        
        if (workout.program.id != programId) {
            throw IllegalArgumentException("Workout does not belong to the specified program")
        }

        val deactivatedWorkout = workout.copy(isActive = false, updatedAt = LocalDateTime.now())
        programWorkoutRepository.save(deactivatedWorkout)
    }

    fun duplicateProgramWorkout(programId: Long, workoutId: Long, request: DuplicateWorkoutRequest): ProgramWorkoutDto {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }
        
        val originalWorkout = programWorkoutRepository.findByIdWithExercises(workoutId)
            ?: throw IllegalArgumentException("Workout not found with id: $workoutId")
        
        if (originalWorkout.program.id != programId) {
            throw IllegalArgumentException("Workout does not belong to the specified program")
        }

        val duplicateWorkout = ProgramWorkout(
            program = program,
            name = request.name,
            description = originalWorkout.description,
            workoutType = originalWorkout.workoutType,
            estimatedDuration = originalWorkout.estimatedDuration,
            orderInProgram = request.orderInProgram ?: (programWorkoutRepository.countByProgram(program).toInt() + 1),
            notes = originalWorkout.notes,
            warmupInstructions = originalWorkout.warmupInstructions,
            cooldownInstructions = originalWorkout.cooldownInstructions
        )

        val savedWorkout = programWorkoutRepository.save(duplicateWorkout)

        // Duplicate all exercises
        val duplicateExercises = originalWorkout.exercises.map { originalExercise ->
            ProgramWorkoutExercise(
                programWorkout = savedWorkout,
                exercise = originalExercise.exercise,
                sets = originalExercise.sets,
                reps = originalExercise.reps,
                intensityPercentage = originalExercise.intensityPercentage,
                weight = originalExercise.weight,
                distance = originalExercise.distance,
                time = originalExercise.time,
                restTime = originalExercise.restTime,
                orderInWorkout = originalExercise.orderInWorkout,
                notes = originalExercise.notes,
                coachInstructions = originalExercise.coachInstructions,
                progressionType = originalExercise.progressionType,
                progressionValue = originalExercise.progressionValue,
                isSuperset = originalExercise.isSuperset,
                supersetGroup = originalExercise.supersetGroup,
                isDropset = originalExercise.isDropset,
                isFailure = originalExercise.isFailure
            )
        }

        val savedExercises = programWorkoutExerciseRepository.saveAll(duplicateExercises)

        // Create a copy of the saved workout with the saved exercises for DTO conversion
        val workoutWithExercises = savedWorkout.copy(exercises = savedExercises.toList())
        
        return convertToProgramWorkoutDto(workoutWithExercises)
    }

    // Exercise Management Methods
    fun addExerciseToWorkout(programId: Long, workoutId: Long, request: CreateProgramWorkoutExerciseRequest): ProgramWorkoutExerciseDto {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }
        
        val workout = programWorkoutRepository.findById(workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $workoutId") }
        
        if (workout.program.id != programId) {
            throw IllegalArgumentException("Workout does not belong to the specified program")
        }

        val exercise = exerciseRepository.findById(request.exerciseId)
            .orElseThrow { IllegalArgumentException("Exercise not found with id: ${request.exerciseId}") }

        val workoutExercise = ProgramWorkoutExercise(
            programWorkout = workout,
            exercise = exercise,
            sets = request.sets,
            reps = request.reps,
            intensityPercentage = request.intensityPercentage,
            weight = request.weight,
            distance = request.distance,
            time = request.time,
            restTime = request.restTime,
            orderInWorkout = request.orderInWorkout,
            notes = request.notes,
            coachInstructions = request.coachInstructions,
            progressionType = request.progressionType?.let { ProgressionType.valueOf(it) },
            progressionValue = request.progressionValue,
            isSuperset = request.isSuperset,
            supersetGroup = request.supersetGroup,
            isDropset = request.isDropset,
            isFailure = request.isFailure
        )

        val savedExercise = programWorkoutExerciseRepository.save(workoutExercise)
        return convertToProgramWorkoutExerciseDto(savedExercise)
    }

    fun updateWorkoutExercise(programId: Long, workoutId: Long, exerciseId: Long, request: UpdateProgramWorkoutExerciseRequest): ProgramWorkoutExerciseDto {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }
        
        val workout = programWorkoutRepository.findById(workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $workoutId") }
        
        if (workout.program.id != programId) {
            throw IllegalArgumentException("Workout does not belong to the specified program")
        }

        val existingExercise = programWorkoutExerciseRepository.findById(exerciseId)
            .orElseThrow { IllegalArgumentException("Exercise not found with id: $exerciseId") }
        
        if (existingExercise.programWorkout.id != workoutId) {
            throw IllegalArgumentException("Exercise does not belong to the specified workout")
        }

        val updatedExercise = existingExercise.copy(
            sets = request.sets ?: existingExercise.sets,
            reps = request.reps ?: existingExercise.reps,
            intensityPercentage = request.intensityPercentage ?: existingExercise.intensityPercentage,
            weight = request.weight ?: existingExercise.weight,
            distance = request.distance ?: existingExercise.distance,
            time = request.time ?: existingExercise.time,
            restTime = request.restTime ?: existingExercise.restTime,
            orderInWorkout = request.orderInWorkout ?: existingExercise.orderInWorkout,
            notes = request.notes ?: existingExercise.notes,
            coachInstructions = request.coachInstructions ?: existingExercise.coachInstructions,
            progressionType = request.progressionType?.let { ProgressionType.valueOf(it) } ?: existingExercise.progressionType,
            progressionValue = request.progressionValue ?: existingExercise.progressionValue,
            isSuperset = request.isSuperset ?: existingExercise.isSuperset,
            supersetGroup = request.supersetGroup ?: existingExercise.supersetGroup,
            isDropset = request.isDropset ?: existingExercise.isDropset,
            isFailure = request.isFailure ?: existingExercise.isFailure,
            updatedAt = LocalDateTime.now()
        )

        val savedExercise = programWorkoutExerciseRepository.save(updatedExercise)
        return convertToProgramWorkoutExerciseDto(savedExercise)
    }

    fun removeExerciseFromWorkout(programId: Long, workoutId: Long, exerciseId: Long) {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }
        
        val workout = programWorkoutRepository.findById(workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $workoutId") }
        
        if (workout.program.id != programId) {
            throw IllegalArgumentException("Workout does not belong to the specified program")
        }

        val exercise = programWorkoutExerciseRepository.findById(exerciseId)
            .orElseThrow { IllegalArgumentException("Exercise not found with id: $exerciseId") }
        
        if (exercise.programWorkout.id != workoutId) {
            throw IllegalArgumentException("Exercise does not belong to the specified workout")
        }

        programWorkoutExerciseRepository.delete(exercise)
    }

    fun reorderWorkoutExercises(programId: Long, workoutId: Long, request: ReorderExercisesRequest) {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }
        
        val workout = programWorkoutRepository.findById(workoutId)
            .orElseThrow { IllegalArgumentException("Workout not found with id: $workoutId") }
        
        if (workout.program.id != programId) {
            throw IllegalArgumentException("Workout does not belong to the specified program")
        }

        // Update order for each exercise
        request.exerciseOrders.forEach { orderItem ->
            val exercise = programWorkoutExerciseRepository.findById(orderItem.exerciseId)
                .orElseThrow { IllegalArgumentException("Exercise not found with id: ${orderItem.exerciseId}") }
            
            if (exercise.programWorkout.id != workoutId) {
                throw IllegalArgumentException("Exercise ${orderItem.exerciseId} does not belong to workout $workoutId")
            }

            val updatedExercise = exercise.copy(
                orderInWorkout = orderItem.newOrder,
                updatedAt = LocalDateTime.now()
            )
            programWorkoutExerciseRepository.save(updatedExercise)
        }
    }

    fun reorderProgramWorkouts(programId: Long, request: ReorderWorkoutsRequest) {
        val program = programRepository.findById(programId)
            .orElseThrow { IllegalArgumentException("Program not found with id: $programId") }
        
        // Validate all workout IDs belong to the program and update their order
        request.workoutOrders.forEach { orderItem ->
            val workout = programWorkoutRepository.findById(orderItem.workoutId)
                .orElseThrow { IllegalArgumentException("Workout not found with id: ${orderItem.workoutId}") }
            
            if (workout.program.id != programId) {
                throw IllegalArgumentException("Workout ${orderItem.workoutId} does not belong to program $programId")
            }
            
            val updatedWorkout = workout.copy(
                orderInProgram = orderItem.newOrder,
                updatedAt = LocalDateTime.now()
            )
            programWorkoutRepository.save(updatedWorkout)
        }
    }

    fun initializeDefaultPrograms() {
        if (programRepository.count() == 0L) {
            // We'll need to create a default user first for this to work
            // For now, this is a placeholder for when user management is implemented
        }
    }
}

// Request DTOs for basic program management
data class CreateBasicProgramRequest(
    val name: String,
    val description: String? = null,
    val sport: Sport,
    val durationWeeks: Int? = null,
    val difficultyLevel: String? = null,
    val goals: String? = null,
    val createdById: Long,
    val isTemplate: Boolean? = false
)

data class UpdateProgramRequest(
    val name: String? = null,
    val description: String? = null,
    val durationWeeks: Int? = null,
    val difficultyLevel: String? = null,
    val goals: String? = null
)

data class DuplicateProgramRequest(
    val name: String? = null,
    val createdById: Long,
    val isTemplate: Boolean? = null
)

// Response DTOs
