package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "programs")
data class Program(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val sport: Sport,
    
    @Column
    val durationWeeks: Int? = null,
    
    @Column
    val difficultyLevel: String? = null, // Beginner, Intermediate, Advanced
    
    @Column(columnDefinition = "TEXT")
    val goals: String? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @JsonIgnore
    val createdBy: User,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    val isTemplate: Boolean = false,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "program", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val programWorkouts: List<ProgramWorkout> = emptyList()
)