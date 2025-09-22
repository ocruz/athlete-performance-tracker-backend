package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "assessment_results")
data class AssessmentResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    @JsonIgnore
    val assessment: Assessment,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    @JsonIgnore
    val athlete: Athlete,
    
    @Column(nullable = false)
    val testDate: LocalDate,
    
    @Column(nullable = false, name = "result_value")
    val value: Double,
    
    @Column
    val rawValue: String? = null, // For storing raw/original values (e.g., "2:30" for time)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AssessmentStatus = AssessmentStatus.COMPLETED,
    
    @Column(columnDefinition = "TEXT")
    val notes: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val conditions: String? = null, // Environmental conditions, equipment used, etc.
    
    @Column
    val percentileRank: Double? = null, // Percentile ranking compared to peers
    
    @Column
    val improvementFromBaseline: Double? = null, // Improvement from athlete's baseline
    
    @Column
    val improvementPercentage: Double? = null, // Percentage improvement
    
    @Column
    val isBaseline: Boolean = false, // Whether this is the athlete's baseline result
    
    @Column
    val videoUrl: String? = null, // Optional video recording of the assessment
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conducted_by_id")
    @JsonIgnore
    val conductedBy: User? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_schedule_id")
    @JsonIgnore
    val assessmentSchedule: AssessmentSchedule? = null, // Link to scheduled assessment if applicable
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class AssessmentStatus {
    SCHEDULED,      // Assessment is scheduled but not completed
    IN_PROGRESS,    // Assessment is currently being conducted
    COMPLETED,      // Assessment completed successfully
    INCOMPLETE,     // Assessment started but not finished
    CANCELLED,      // Assessment was cancelled
    NO_SHOW,        // Athlete didn't show up for scheduled assessment
    RESCHEDULED     // Assessment was rescheduled
}