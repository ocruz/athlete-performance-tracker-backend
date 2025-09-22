package com.athletetracker.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "exercises")
data class Exercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, unique = true)
    val name: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: ExerciseCategory,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val muscleGroup: MuscleGroup,
    
    @Column
    val instructions: String? = null,
    
    @Column
    val videoUrl: String? = null,
    
    @Column
    val imageUrl: String? = null,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ExerciseCategory {
    STRENGTH,
    CARDIO,
    FLEXIBILITY,
    PLYOMETRIC,
    AGILITY,
    BALANCE,
    SPORT_SPECIFIC
}

enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    ARMS,
    LEGS,
    CORE,
    FULL_BODY,
    GLUTES,
    CALVES,
    CARDIO
}