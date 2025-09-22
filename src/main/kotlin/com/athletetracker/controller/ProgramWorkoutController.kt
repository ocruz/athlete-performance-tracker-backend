package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.service.ProgramService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/programs/{programId}/workouts")
@CrossOrigin(origins = ["http://localhost:3000"])
class ProgramWorkoutController(
    private val programService: ProgramService
) {

    @PostMapping
    fun addWorkoutToProgram(
        @PathVariable programId: Long,
        @Valid @RequestBody request: CreateProgramWorkoutRequest
    ): ResponseEntity<ProgramWorkoutDto> {
        val workout = programService.addWorkoutToProgram(programId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(workout)
    }

    @GetMapping
    fun getProgramWorkouts(@PathVariable programId: Long): ResponseEntity<List<ProgramWorkoutDto>> {
        val workouts = programService.getProgramWorkouts(programId)
        return ResponseEntity.ok(workouts)
    }

    @PutMapping("/{workoutId}")
    fun updateProgramWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @Valid @RequestBody request: UpdateProgramWorkoutRequest
    ): ResponseEntity<ProgramWorkoutDto> {
        val workout = programService.updateProgramWorkout(programId, workoutId, request)
        return ResponseEntity.ok(workout)
    }

    @DeleteMapping("/{workoutId}")
    fun deleteProgramWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long
    ): ResponseEntity<Void> {
        programService.deleteProgramWorkout(programId, workoutId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{workoutId}/duplicate")
    fun duplicateProgramWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @Valid @RequestBody request: DuplicateWorkoutRequest
    ): ResponseEntity<ProgramWorkoutDto> {
        val workout = programService.duplicateProgramWorkout(programId, workoutId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(workout)
    }

    @PutMapping("/reorder")
    fun reorderProgramWorkouts(
        @PathVariable programId: Long,
        @Valid @RequestBody request: ReorderWorkoutsRequest
    ): ResponseEntity<Void> {
        programService.reorderProgramWorkouts(programId, request)
        return ResponseEntity.ok().build()
    }

    // Exercise Management Endpoints
    @PostMapping("/{workoutId}/exercises")
    fun addExerciseToWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @Valid @RequestBody request: CreateProgramWorkoutExerciseRequest
    ): ResponseEntity<ProgramWorkoutExerciseDto> {
        val exercise = programService.addExerciseToWorkout(programId, workoutId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(exercise)
    }

    @PutMapping("/{workoutId}/exercises/{exerciseId}")
    fun updateWorkoutExercise(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @PathVariable exerciseId: Long,
        @Valid @RequestBody request: UpdateProgramWorkoutExerciseRequest
    ): ResponseEntity<ProgramWorkoutExerciseDto> {
        val exercise = programService.updateWorkoutExercise(programId, workoutId, exerciseId, request)
        return ResponseEntity.ok(exercise)
    }

    @DeleteMapping("/{workoutId}/exercises/{exerciseId}")
    fun removeExerciseFromWorkout(
        @PathVariable programId: Long,
        @PathVariable workoutId: Long,
        @PathVariable exerciseId: Long
    ): ResponseEntity<Void> {
        programService.removeExerciseFromWorkout(programId, workoutId, exerciseId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{workoutId}/exercises/reorder")
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