package com.athletetracker.controller

import com.athletetracker.entity.Exercise
import com.athletetracker.entity.ExerciseCategory
import com.athletetracker.entity.MuscleGroup
import com.athletetracker.service.CreateExerciseRequest
import com.athletetracker.service.ExerciseService
import com.athletetracker.service.UpdateExerciseRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/exercises")
class ExerciseController(
    private val exerciseService: ExerciseService
) {

    @PostMapping
    fun createExercise(@RequestBody request: CreateExerciseRequest): ResponseEntity<Exercise> {
        val exercise = exerciseService.createExercise(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(exercise)
    }

    @GetMapping("/{id}")
    fun getExerciseById(@PathVariable id: Long): ResponseEntity<Exercise> {
        val exercise = exerciseService.getExerciseById(id)
        return ResponseEntity.ok(exercise)
    }

    @GetMapping
    fun getAllActiveExercises(): ResponseEntity<List<Exercise>> {
        val exercises = exerciseService.getAllActiveExercises()
        return ResponseEntity.ok(exercises)
    }

    @GetMapping("/category/{category}")
    fun getExercisesByCategory(@PathVariable category: ExerciseCategory): ResponseEntity<List<Exercise>> {
        val exercises = exerciseService.getExercisesByCategory(category)
        return ResponseEntity.ok(exercises)
    }

    @GetMapping("/muscle-group/{muscleGroup}")
    fun getExercisesByMuscleGroup(@PathVariable muscleGroup: MuscleGroup): ResponseEntity<List<Exercise>> {
        val exercises = exerciseService.getExercisesByMuscleGroup(muscleGroup)
        return ResponseEntity.ok(exercises)
    }

    @GetMapping("/search")
    fun searchExercises(@RequestParam query: String): ResponseEntity<List<Exercise>> {
        val exercises = exerciseService.searchExercises(query)
        return ResponseEntity.ok(exercises)
    }

    @PutMapping("/{id}")
    fun updateExercise(
        @PathVariable id: Long,
        @RequestBody request: UpdateExerciseRequest
    ): ResponseEntity<Exercise> {
        val exercise = exerciseService.updateExercise(id, request)
        return ResponseEntity.ok(exercise)
    }

    @DeleteMapping("/{id}")
    fun deleteExercise(@PathVariable id: Long): ResponseEntity<Void> {
        exerciseService.deleteExercise(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/initialize-defaults")
    fun initializeDefaultExercises(): ResponseEntity<String> {
        exerciseService.initializeDefaultExercises()
        return ResponseEntity.ok("Default exercises initialized successfully")
    }

    @GetMapping("/categories")
    fun getExerciseCategories(): ResponseEntity<List<ExerciseCategory>> {
        return ResponseEntity.ok(ExerciseCategory.values().toList())
    }

    @GetMapping("/muscle-groups")
    fun getMuscleGroups(): ResponseEntity<List<MuscleGroup>> {
        return ResponseEntity.ok(MuscleGroup.values().toList())
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }
}