package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "athlete_workouts")
data class AthleteWorkout(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    val athlete: Athlete,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    val coach: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_workout_id")
    @JsonIgnore
    val programWorkout: ProgramWorkout? = null, // Link to workout template (optional)
    
    @Column(nullable = false)
    val workoutDate: LocalDateTime,
    
    @Column
    val name: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val notes: String? = null,
    
    @Column
    val rpe: Int? = null, // Rate of Perceived Exertion (1-10)
    
    @Column
    val duration: Int? = null, // in minutes
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "athleteWorkout", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val athleteWorkoutExercises: List<AthleteWorkoutExercise> = emptyList()
)

enum class ExerciseCompletionStatus {
    PLANNED,        // Exercise is planned but not yet performed
    IN_PROGRESS,    // Exercise is currently being performed
    COMPLETED,      // Exercise completed as planned
    MODIFIED,       // Exercise completed but with modifications
    SKIPPED,        // Exercise was skipped
    FAILED          // Exercise attempted but failed to complete
}