package com.athletetracker.controller

import com.athletetracker.dto.AthleteWorkoutDto
import com.athletetracker.dto.AthleteWorkoutExerciseDto
import com.athletetracker.entity.AthleteWorkout
import com.athletetracker.service.CompleteWorkoutRequest
import com.athletetracker.service.CreateAthleteWorkoutRequest
import com.athletetracker.service.ExerciseCompletionRequest
import com.athletetracker.service.UpdateWorkoutRequest
import com.athletetracker.service.AthleteProgramWorkoutService
import com.athletetracker.service.WorkoutStatsResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/athlete-workouts")
class AthleteWorkoutController(
    private val athleteProgramWorkoutService: AthleteProgramWorkoutService
) {

    @PostMapping
    fun createWorkout(@RequestBody request: CreateAthleteWorkoutRequest): ResponseEntity<Any> {
        val workout = athleteProgramWorkoutService.createWorkout(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(workout)
    }

    @GetMapping("/{id}")
    fun getWorkoutById(@PathVariable id: Long): ResponseEntity<AthleteWorkoutDto> {
        val workout = athleteProgramWorkoutService.getWorkoutById(id)
        return ResponseEntity.ok(workout)
    }

    @GetMapping("/athlete/{athleteId}")
    fun getWorkoutsByAthlete(@PathVariable athleteId: Long): ResponseEntity<List<AthleteWorkoutDto>> {
        val workouts = athleteProgramWorkoutService.getAthleteWorkoutsByAthlete(athleteId)
        return ResponseEntity.ok(workouts)
    }

    @GetMapping("/coach/{coachId}")
    fun getWorkoutsByCoach(@PathVariable coachId: Long): ResponseEntity<List<AthleteWorkout>> {
        val workouts = athleteProgramWorkoutService.getWorkoutsByCoach(coachId)
        return ResponseEntity.ok(workouts)
    }

    @GetMapping("/athlete/{athleteId}/range")
    fun getWorkoutsByAthleteInDateRange(
        @PathVariable athleteId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<List<AthleteWorkout>> {
        val workouts = athleteProgramWorkoutService.getWorkoutsByAthleteInDateRange(athleteId, startDate, endDate)
        return ResponseEntity.ok(workouts)
    }

    @PutMapping("/{id}")
    fun updateWorkout(
        @PathVariable id: Long,
        @RequestBody request: UpdateWorkoutRequest
    ): ResponseEntity<AthleteWorkout> {
        val workout = athleteProgramWorkoutService.updateWorkout(id, request)
        return ResponseEntity.ok(workout)
    }

    @DeleteMapping("/{id}")
    fun deleteWorkout(@PathVariable id: Long): ResponseEntity<Void> {
        athleteProgramWorkoutService.deleteWorkout(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/athlete/{athleteId}/stats")
    fun getWorkoutStats(@PathVariable athleteId: Long): ResponseEntity<WorkoutStatsResponse> {
        val stats = athleteProgramWorkoutService.getWorkoutStats(athleteId)
        return ResponseEntity.ok(stats)
    }

    @PostMapping("/complete")
    fun completeWorkout(@RequestBody request: CompleteWorkoutRequest): ResponseEntity<AthleteWorkoutDto> {
        val workout = athleteProgramWorkoutService.completeWorkout(request)
        return ResponseEntity.ok(workout)
    }

    @PostMapping("/{workoutId}/start")
    fun startWorkout(@PathVariable workoutId: Long): ResponseEntity<AthleteWorkoutDto> {
        val workout = athleteProgramWorkoutService.startWorkout(workoutId)
        return ResponseEntity.ok(workout)
    }

    @PutMapping("/exercises/{workoutExerciseId}")
    fun updateWorkoutExercise(
        @PathVariable workoutExerciseId: Long,
        @RequestBody request: ExerciseCompletionRequest
    ): ResponseEntity<AthleteWorkoutExerciseDto> {
        val exercise = athleteProgramWorkoutService.updateWorkoutExercise(workoutExerciseId, request)
        return ResponseEntity.ok(exercise)
    }

    @DeleteMapping("/{workoutId}/exercises/{exerciseId}")
    fun removeExerciseFromWorkout(
        @PathVariable workoutId: Long,
        @PathVariable exerciseId: Long
    ): ResponseEntity<Void> {
        athleteProgramWorkoutService.removeExerciseFromWorkout(workoutId, exerciseId)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }
}