package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "assessment_schedules")
data class AssessmentSchedule(
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
    val scheduledDate: LocalDate,
    
    @Column
    val scheduledTime: LocalTime? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ScheduleStatus = ScheduleStatus.SCHEDULED,
    
    @Enumerated(EnumType.STRING)
    @Column
    val recurrenceType: RecurrenceType? = null,
    
    @Column
    val recurrenceInterval: Int? = null, // e.g., every 2 weeks, every 3 months
    
    @Column
    val recurrenceEndDate: LocalDate? = null, // When to stop recurring
    
    @Column
    val maxRecurrences: Int? = null, // Maximum number of recurrences
    
    @Column(columnDefinition = "TEXT")
    val notes: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val specialInstructions: String? = null, // Special instructions for this assessment
    
    @Column
    val location: String? = null, // Where the assessment will be conducted
    
    @Column
    val reminderSent: Boolean = false, // Whether reminder has been sent
    
    @Column
    val reminderDate: LocalDateTime? = null, // When reminder was sent
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_by_id", nullable = false)
    @JsonIgnore
    val scheduledBy: User,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "assessmentSchedule", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val assessmentResults: List<AssessmentResult> = emptyList()
)

enum class ScheduleStatus {
    SCHEDULED,      // Assessment is scheduled
    CONFIRMED,      // Assessment confirmed by athlete/coach
    COMPLETED,      // Assessment has been completed
    CANCELLED,      // Assessment was cancelled
    RESCHEDULED,    // Assessment was rescheduled
    NO_SHOW,        // Athlete didn't show up
    IN_PROGRESS     // Assessment is currently happening
}

enum class RecurrenceType {
    DAILY,          // Every day
    WEEKLY,         // Every week
    BIWEEKLY,       // Every two weeks
    MONTHLY,        // Every month
    QUARTERLY,      // Every quarter (3 months)
    SEMI_ANNUALLY,  // Twice a year (6 months)
    ANNUALLY,       // Once a year
    CUSTOM          // Custom recurrence pattern
}