package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "program_progress")
data class ProgramProgress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_program_id", nullable = false)
    @JsonIgnore
    val athleteProgram: AthleteProgram,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_workout_exercise_id", nullable = false)
    @JsonIgnore
    val programWorkoutExercise: ProgramWorkoutExercise,

    @Column(nullable = false)
    val completedDate: LocalDate,

    @Column
    val actualSets: Int? = null,

    @Column
    val actualReps: Int? = null,

    @Column
    val actualWeight: Double? = null,

    @Column
    val actualIntensity: Double? = null,

    @Column
    val restTime: Int? = null, // in seconds

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val completionStatus: CompletionStatus = CompletionStatus.COMPLETED,

    @Column(columnDefinition = "TEXT")
    val athleteNotes: String? = null,

    @Column(columnDefinition = "TEXT")
    val coachNotes: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logged_by_id", nullable = false)
    @JsonIgnore
    val loggedBy: User
)

enum class CompletionStatus {
    COMPLETED,
}