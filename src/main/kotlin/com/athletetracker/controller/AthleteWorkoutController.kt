package com.athletetracker.controller

import com.athletetracker.dto.*
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
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * Consolidated controller for workout execution and tracking.
 * Handles actual workout instances (not templates).
 */
@RestController
@RequestMapping("/workouts")
@CrossOrigin(origins = ["http://localhost:3000"])
class WorkoutController(
    private val athleteProgramWorkoutService: AthleteProgramWorkoutService
) {

    @PostMapping
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun createWorkout(@RequestBody request: CreateAthleteWorkoutRequest): ResponseEntity<AthleteWorkoutDto> {
        val workout = athleteProgramWorkoutService.createWorkout(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(workout)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('COACH') or hasRole('ATHLETE') or hasRole('ADMIN')")
    fun getWorkoutById(@PathVariable id: Long): ResponseEntity<AthleteWorkoutDto> {
        val workout = athleteProgramWorkoutService.getWorkoutById(id)
        return ResponseEntity.ok(workout)
    }

    @GetMapping("/athlete/{athleteId}")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN') or hasRole('ATHLETE')")
    fun getWorkoutsByAthlete(
        @PathVariable athleteId: Long,
        authentication: Authentication
    ): ResponseEntity<List<AthleteWorkoutDto>> {
        // TODO: Add security check for athletes to only access their own workouts
        // For now, allowing access for troubleshooting
        val workouts = athleteProgramWorkoutService.getAthleteWorkoutsByAthlete(athleteId)
        return ResponseEntity.ok(workouts)
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun getWorkoutsByCoach(@PathVariable coachId: Long): ResponseEntity<List<AthleteWorkoutDto>> {
        val workouts = athleteProgramWorkoutService.getWorkoutsByCoach(coachId)
        return ResponseEntity.ok(workouts)
    }

    @GetMapping("/athlete/{athleteId}/range")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun getWorkoutsByAthleteInDateRange(
        @PathVariable athleteId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<List<AthleteWorkoutDto>> {
        val workouts = athleteProgramWorkoutService.getWorkoutsByAthleteInDateRange(athleteId, startDate, endDate)
        return ResponseEntity.ok(workouts)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COACH') or hasRole('ATHLETE') or hasRole('ADMIN')")
    fun updateWorkout(
        @PathVariable id: Long,
        @RequestBody request: UpdateWorkoutRequest
    ): ResponseEntity<AthleteWorkoutDto> {
        val workout = athleteProgramWorkoutService.updateWorkout(id, request)
        return ResponseEntity.ok(workout)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
    fun deleteWorkout(@PathVariable id: Long): ResponseEntity<Void> {
        athleteProgramWorkoutService.deleteWorkout(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/athlete/{athleteId}/stats")
    @PreAuthorize("hasRole('COACH') or hasRole('ATHLETE') or hasRole('ADMIN')")
    fun getWorkoutStats(@PathVariable athleteId: Long): ResponseEntity<WorkoutStatsResponse> {
        val stats = athleteProgramWorkoutService.getWorkoutStats(athleteId)
        return ResponseEntity.ok(stats)
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('ATHLETE') or hasRole('COACH')")
    fun completeWorkout(@RequestBody request: CompleteWorkoutRequest): ResponseEntity<AthleteWorkoutDto> {
        val workout = athleteProgramWorkoutService.completeWorkout(request)
        return ResponseEntity.ok(workout)
    }

    @PostMapping("/{workoutId}/start")
    @PreAuthorize("hasRole('ATHLETE')")
    fun startWorkout(@PathVariable workoutId: Long): ResponseEntity<AthleteWorkoutDto> {
        val workout = athleteProgramWorkoutService.startWorkout(workoutId)
        return ResponseEntity.ok(workout)
    }


    @PutMapping("/exercises/{workoutExerciseId}")
    @PreAuthorize("hasRole('ATHLETE') or hasRole('COACH')")
    fun updateWorkoutExercise(
        @PathVariable workoutExerciseId: Long,
        @RequestBody request: ExerciseCompletionRequest
    ): ResponseEntity<AthleteWorkoutExerciseDto> {
        val exercise = athleteProgramWorkoutService.updateWorkoutExercise(workoutExerciseId, request)
        return ResponseEntity.ok(exercise)
    }

    @DeleteMapping("/{workoutId}/exercises/{exerciseId}")
    @PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
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