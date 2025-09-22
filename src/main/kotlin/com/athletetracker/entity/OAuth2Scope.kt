package com.athletetracker.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "oauth2_scopes")
data class OAuth2Scope(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "scope_name", unique = true, nullable = false)
    val scopeName: String,
    
    @Column(name = "display_name", nullable = false)
    val displayName: String,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val description: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: ScopeCategory,
    
    @Column(name = "is_sensitive", nullable = false)
    val isSensitive: Boolean = false, // Requires additional consent
    
    @Column(name = "is_default", nullable = false)
    val isDefault: Boolean = false, // Included by default in client registrations
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ScopeCategory {
    ATHLETE_DATA,  // Basic athlete profile and contact information
    PERFORMANCE,   // Performance metrics and progress data
    WORKOUTS,      // Workout history and exercise data
    ASSESSMENTS,   // Assessment results and fitness testing
    PROGRAMS       // Training programs and schedules
}