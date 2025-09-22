package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "program_workout_exercises")
data class ProgramWorkoutExercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_workout_id", nullable = false)
    @JsonIgnore
    val programWorkout: ProgramWorkout,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @JsonIgnore
    val exercise: Exercise,
    
    @Column
    val sets: Int? = null,
    
    @Column
    val reps: Int? = null,
    
    @Column
    val intensityPercentage: Double? = null, // % of 1RM
    
    @Column
    val weight: Double? = null, // Specific weight if not using percentage
    
    @Column
    val distance: Double? = null, // For cardio exercises (miles/meters)
    
    @Column
    val time: Int? = null, // Duration in seconds
    
    @Column
    val restTime: Int? = null, // Rest between sets in seconds
    
    @Column(nullable = false)
    val orderInWorkout: Int = 0, // Order within the workout
    
    @Column(columnDefinition = "TEXT")
    val notes: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val coachInstructions: String? = null, // Special instructions for this exercise
    
    @Enumerated(EnumType.STRING)
    @Column
    val progressionType: ProgressionType? = null, // How this exercise should progress
    
    @Column
    val progressionValue: Double? = null, // Amount to progress (weight, reps, time)
    
    @Column
    val isSuperset: Boolean = false, // Part of a superset
    
    @Column
    val supersetGroup: Int? = null, // Group ID for superset exercises
    
    @Column
    val isDropset: Boolean = false, // Uses drop sets
    
    @Column
    val isFailure: Boolean = false, // Should be performed to failure
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ProgressionType {
    WEIGHT,         // Increase weight
    REPS,           // Increase repetitions
    SETS,           // Increase sets
    TIME,           // Increase duration
    DISTANCE,       // Increase distance
    REST_DECREASE,  // Decrease rest time
    INTENSITY,      // Increase intensity percentage
    NONE           // No automatic progression
}