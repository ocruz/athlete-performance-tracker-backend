package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "athlete_workout_exercises")
data class AthleteWorkoutExercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    @JsonIgnore
    val athleteWorkout: AthleteWorkout,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    val exercise: Exercise,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_workout_exercise_id")
    @JsonIgnore
    val programWorkoutExercise: ProgramWorkoutExercise? = null, // Link to workout template exercise

    // Planned values (from template)
    @Column
    val plannedSets: Int? = null,

    @Column
    val plannedReps: Int? = null,

    @Column
    val plannedWeight: Double? = null,

    @Column
    val plannedDistance: Double? = null,

    @Column
    val plannedTime: Int? = null,

    @Column
    val plannedRestTime: Int? = null,

    @Column
    val plannedIntensity: Double? = null, // % of 1RM

    // Actual performed values
    @Column
    val actualSets: Int? = null,

    @Column
    val actualReps: Int? = null,

    @Column
    val actualWeight: Double? = null,

    @Column
    val actualDistance: Double? = null,

    @Column
    val actualTime: Int? = null,

    @Column
    val actualRestTime: Int? = null,

    @Column
    val actualIntensity: Double? = null,

    // Legacy fields for backward compatibility
    @Column
    val sets: Int? = null,

    @Column
    val reps: Int? = null,

    @Column
    val weight: Double? = null,

    @Column
    val distance: Double? = null,

    @Column
    val time: Int? = null,

    @Column
    val restTime: Int? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(nullable = false)
    val orderInWorkout: Int = 0,

    @Column
    val isFromProgram: Boolean = false, // Whether this exercise came from a program template

    @Enumerated(EnumType.STRING)
    @Column
    val completionStatus: ExerciseCompletionStatus = ExerciseCompletionStatus.PLANNED,

    @Column
    val rpe: Int? = null, // Rate of Perceived Exertion for this exercise (1-10)

    @Column(name = "is_pr", nullable = false)
    val isPR: Boolean = false // Whether this exercise result is a personal record
)