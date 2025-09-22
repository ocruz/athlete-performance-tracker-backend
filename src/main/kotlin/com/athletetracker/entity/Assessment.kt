package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "assessments")
data class Assessment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, unique = true)
    val name: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: AssessmentCategory,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: AssessmentType,
    
    @Column(columnDefinition = "TEXT")
    val instructions: String? = null,
    
    @Column
    val unit: String? = null, // e.g., "seconds", "inches", "lbs", "reps"
    
    @Column
    val scoringType: String? = null, // e.g., "lower_better", "higher_better", "target_range"
    
    @Column
    val targetValue: Double? = null, // Optional target value for assessment
    
    @Column
    val minValue: Double? = null, // Minimum acceptable value
    
    @Column
    val maxValue: Double? = null, // Maximum acceptable value
    
    @Column
    val equipmentRequired: String? = null, // Equipment needed for the assessment
    
    @Column
    val estimatedDuration: Int? = null, // Estimated duration in minutes
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val sport: Sport,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @JsonIgnore
    val createdBy: User,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    val isTemplate: Boolean = true, // True for standard assessments, false for custom ones
    
    @Column(nullable = false)
    val generatePerformanceMetric: Boolean = false, // Whether to auto-generate performance metrics from results
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "assessment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val assessmentResults: List<AssessmentResult> = emptyList(),
    
    @OneToMany(mappedBy = "assessment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val assessmentSchedules: List<AssessmentSchedule> = emptyList()
)

enum class AssessmentCategory {
    FITNESS_TEST,           // General fitness assessments
    STRENGTH_TEST,          // Strength-based assessments
    SPEED_AGILITY,          // Speed and agility tests
    ENDURANCE,              // Cardiovascular endurance tests
    FLEXIBILITY_MOBILITY,   // Flexibility and mobility assessments
    BALANCE_COORDINATION,   // Balance and coordination tests
    BODY_COMPOSITION,       // Body composition measurements
    SPORT_SPECIFIC,         // Sport-specific skill assessments
    FUNCTIONAL_MOVEMENT,    // Functional movement screening
    INJURY_PREVENTION,      // Injury prevention assessments
    CUSTOM                  // Custom assessments
}

enum class AssessmentType {
    TIMED,                  // Time-based assessments (e.g., 40-yard dash)
    DISTANCE,               // Distance-based assessments (e.g., broad jump)
    REPETITION,             // Repetition-based assessments (e.g., push-ups)
    WEIGHT,                 // Weight-based assessments (e.g., 1RM)
    SCORE,                  // Score-based assessments (e.g., skill ratings)
    MEASUREMENT,            // Measurement-based assessments (e.g., height, reach)
    PERCENTAGE,             // Percentage-based assessments (e.g., body fat)
    QUALITATIVE             // Qualitative assessments (e.g., movement quality)
}