package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.Sport
import com.athletetracker.service.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/programs")
class ProgramController(
    private val programService: ProgramService
) {

    @PostMapping
    fun createBasicProgram(@RequestBody request: CreateBasicProgramRequest): ResponseEntity<ProgramDto> {
        val program = programService.createBasicProgram(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(program)
    }

    @GetMapping("/{id}")
    fun getProgramById(@PathVariable id: Long): ResponseEntity<ProgramDetailResponse> {
        val program = programService.getProgramById(id)
        return ResponseEntity.ok(program)
    }

    @GetMapping
    fun getAllActivePrograms(): ResponseEntity<List<ProgramDto>> {
        val programs = programService.getAllActivePrograms()
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/templates")
    fun getProgramTemplates(): ResponseEntity<List<ProgramDto>> {
        val templates = programService.getProgramTemplates()
        return ResponseEntity.ok(templates)
    }

    @GetMapping("/sport/{sport}")
    fun getProgramsBySport(@PathVariable sport: Sport): ResponseEntity<List<ProgramDto>> {
        val programs = programService.getProgramsBySport(sport)
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/sport/{sport}/templates")
    fun getProgramTemplatesBySport(@PathVariable sport: Sport): ResponseEntity<List<ProgramDto>> {
        val templates = programService.getProgramTemplatesBySport(sport)
        return ResponseEntity.ok(templates)
    }

    @GetMapping("/creator/{creatorId}")
    fun getProgramsByCreator(@PathVariable creatorId: Long): ResponseEntity<List<ProgramDto>> {
        val programs = programService.getProgramsByCreator(creatorId)
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/search")
    fun searchPrograms(@RequestParam query: String): ResponseEntity<List<ProgramDto>> {
        val programs = programService.searchPrograms(query)
        return ResponseEntity.ok(programs)
    }

    @PutMapping("/{id}")
    fun updateProgram(
        @PathVariable id: Long,
        @RequestBody request: UpdateProgramRequest
    ): ResponseEntity<ProgramDto> {
        val program = programService.updateProgram(id, request)
        return ResponseEntity.ok(program)
    }

    @DeleteMapping("/{id}")
    fun deleteProgram(@PathVariable id: Long): ResponseEntity<Void> {
        programService.deleteProgram(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/duplicate")
    fun duplicateProgram(
        @PathVariable id: Long,
        @RequestBody request: DuplicateProgramRequest
    ): ResponseEntity<ProgramDto> {
        val program = programService.duplicateProgram(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(program)
    }

    @PostMapping("/initialize-defaults")
    fun initializeDefaultPrograms(): ResponseEntity<String> {
        programService.initializeDefaultPrograms()
        return ResponseEntity.ok("Default programs initialized successfully")
    }

    // ===== WORKOUT TEMPLATE MANAGEMENT ENDPOINTS =====

    @PostMapping("/{programId}/workouts")
    fun addWorkoutToProgram(
        @PathVariable programId: Long,
        @Valid @RequestBody request: CreateProgramWorkoutRequest
    ): ResponseEntity<ProgramWorkoutDto> {
        val workout = programService.addWorkoutToProgram(programId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(workout)
    }

    @GetMapping("/{programId}/workouts")
    fun getProgramWorkouts(@PathVariable programId: Long): ResponseEntity<List<ProgramWorkoutDto>> {
        val workouts = programService.getProgramWorkouts(programId)
        return ResponseEntity.ok(workouts)
    }

    @PutMapping("/{programId}/workouts/{workoutId}")
    fun updateProgramWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @Valid @RequestBody request: UpdateProgramWorkoutRequest
    ): ResponseEntity<ProgramWorkoutDto> {
        val workout = programService.updateProgramWorkout(programId, workoutId, request)
        return ResponseEntity.ok(workout)
    }

    @DeleteMapping("/{programId}/workouts/{workoutId}")
    fun deleteProgramWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long
    ): ResponseEntity<Void> {
        programService.deleteProgramWorkout(programId, workoutId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{programId}/workouts/{workoutId}/duplicate")
    fun duplicateProgramWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @Valid @RequestBody request: DuplicateWorkoutRequest
    ): ResponseEntity<ProgramWorkoutDto> {
        val workout = programService.duplicateProgramWorkout(programId, workoutId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(workout)
    }

    @PutMapping("/{programId}/workouts/reorder")
    fun reorderProgramWorkouts(
        @PathVariable programId: Long,
        @Valid @RequestBody request: ReorderWorkoutsRequest
    ): ResponseEntity<Void> {
        programService.reorderProgramWorkouts(programId, request)
        return ResponseEntity.ok().build()
    }

    // ===== EXERCISE MANAGEMENT ENDPOINTS =====

    @PostMapping("/{programId}/workouts/{workoutId}/exercises")
    fun addExerciseToWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @Valid @RequestBody request: CreateProgramWorkoutExerciseRequest
    ): ResponseEntity<ProgramWorkoutExerciseDto> {
        val exercise = programService.addExerciseToWorkout(programId, workoutId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(exercise)
    }

    @PutMapping("/{programId}/workouts/{workoutId}/exercises/{exerciseId}")
    fun updateWorkoutExercise(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @PathVariable exerciseId: Long,
        @Valid @RequestBody request: UpdateProgramWorkoutExerciseRequest
    ): ResponseEntity<ProgramWorkoutExerciseDto> {
        val exercise = programService.updateWorkoutExercise(programId, workoutId, exerciseId, request)
        return ResponseEntity.ok(exercise)
    }

    @DeleteMapping("/{programId}/workouts/{workoutId}/exercises/{exerciseId}")
    fun removeExerciseFromWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @PathVariable exerciseId: Long
    ): ResponseEntity<Void> {
        programService.removeExerciseFromWorkout(programId, workoutId, exerciseId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{programId}/workouts/{workoutId}/exercises/reorder")
    fun reorderWorkoutExercises(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @Valid @RequestBody request: ReorderExercisesRequest
    ): ResponseEntity<Void> {
        programService.reorderWorkoutExercises(programId, workoutId, request)
        return ResponseEntity.ok().build()
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }
}