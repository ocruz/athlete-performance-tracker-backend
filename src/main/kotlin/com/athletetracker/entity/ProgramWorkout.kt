package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "program_workouts")
data class ProgramWorkout(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    @JsonIgnore
    val program: Program,
    
    @Column(nullable = false)
    val name: String, // "Upper Body", "Speed Training", "Leg Day"
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val workoutType: WorkoutType,
    
    @Column
    val estimatedDuration: Int? = null, // in minutes
    
    @Column(nullable = false)
    val orderInProgram: Int = 0, // Order within the program
    
    @Column(columnDefinition = "TEXT")
    val notes: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val warmupInstructions: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val cooldownInstructions: String? = null,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "programWorkout", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val exercises: List<ProgramWorkoutExercise> = emptyList()
)

enum class WorkoutType {
    STRENGTH,           // Weight training, resistance exercises
    CARDIO,            // Running, cycling, aerobic exercises
    HIIT,              // High-intensity interval training
    PLYOMETRIC,        // Jump training, explosive movements
    MOBILITY,          // Stretching, yoga, flexibility
    SPORT_SPECIFIC,    // Sport-specific drills and skills
    RECOVERY,          // Active recovery, light movement
    ASSESSMENT,        // Testing days, measurements
    MIXED              // Combination of multiple types
}