package com.athletetracker.controller

import com.athletetracker.dto.WorkoutDto
import com.athletetracker.dto.WorkoutExerciseDto
import com.athletetracker.entity.Workout
import com.athletetracker.service.CompleteWorkoutRequest
import com.athletetracker.service.CreateWorkoutRequest
import com.athletetracker.service.ExerciseCompletionRequest
import com.athletetracker.service.UpdateWorkoutRequest
import com.athletetracker.service.WorkoutService
import com.athletetracker.service.WorkoutStatsResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/workouts")
class WorkoutController(
    private val workoutService: WorkoutService
) {

    @PostMapping
    fun createWorkout(@RequestBody request: CreateWorkoutRequest): ResponseEntity<Any> {
        val workout = workoutService.createWorkout(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(workout)
    }

    @GetMapping("/{id}")
    fun getWorkoutById(@PathVariable id: Long): ResponseEntity<WorkoutDto> {
        val workout = workoutService.getWorkoutById(id)
        return ResponseEntity.ok(workout)
    }

    @GetMapping("/athlete/{athleteId}")
    fun getWorkoutsByAthlete(@PathVariable athleteId: Long): ResponseEntity<List<Workout>> {
        val workouts = workoutService.getWorkoutsByAthlete(athleteId)
        return ResponseEntity.ok(workouts)
    }

    @GetMapping("/coach/{coachId}")
    fun getWorkoutsByCoach(@PathVariable coachId: Long): ResponseEntity<List<Workout>> {
        val workouts = workoutService.getWorkoutsByCoach(coachId)
        return ResponseEntity.ok(workouts)
    }

    @GetMapping("/athlete/{athleteId}/range")
    fun getWorkoutsByAthleteInDateRange(
        @PathVariable athleteId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<List<Workout>> {
        val workouts = workoutService.getWorkoutsByAthleteInDateRange(athleteId, startDate, endDate)
        return ResponseEntity.ok(workouts)
    }

    @PutMapping("/{id}")
    fun updateWorkout(
        @PathVariable id: Long,
        @RequestBody request: UpdateWorkoutRequest
    ): ResponseEntity<Workout> {
        val workout = workoutService.updateWorkout(id, request)
        return ResponseEntity.ok(workout)
    }

    @DeleteMapping("/{id}")
    fun deleteWorkout(@PathVariable id: Long): ResponseEntity<Void> {
        workoutService.deleteWorkout(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/athlete/{athleteId}/stats")
    fun getWorkoutStats(@PathVariable athleteId: Long): ResponseEntity<WorkoutStatsResponse> {
        val stats = workoutService.getWorkoutStats(athleteId)
        return ResponseEntity.ok(stats)
    }

    @PostMapping("/complete")
    fun completeWorkout(@RequestBody request: CompleteWorkoutRequest): ResponseEntity<WorkoutDto> {
        val workout = workoutService.completeWorkout(request)
        return ResponseEntity.ok(workout)
    }

    @PostMapping("/{workoutId}/start")
    fun startWorkout(@PathVariable workoutId: Long): ResponseEntity<WorkoutDto> {
        val workout = workoutService.startWorkout(workoutId)
        return ResponseEntity.ok(workout)
    }

    @PutMapping("/exercises/{workoutExerciseId}")
    fun updateWorkoutExercise(
        @PathVariable workoutExerciseId: Long,
        @RequestBody request: ExerciseCompletionRequest
    ): ResponseEntity<WorkoutExerciseDto> {
        val exercise = workoutService.updateWorkoutExercise(workoutExerciseId, request)
        return ResponseEntity.ok(exercise)
    }

    @DeleteMapping("/{workoutId}/exercises/{exerciseId}")
    fun removeExerciseFromWorkout(
        @PathVariable workoutId: Long,
        @PathVariable exerciseId: Long
    ): ResponseEntity<Void> {
        workoutService.removeExerciseFromWorkout(workoutId, exerciseId)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }
}