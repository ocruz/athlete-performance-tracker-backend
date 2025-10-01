package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "athletes")
data class Athlete(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val firstName: String,

    @Column(nullable = false)
    val lastName: String,

    @Column(nullable = false)
    val dateOfBirth: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val sport: Sport,

    @Column
    val position: String? = null,

    @Column
    val height: Double? = null, // in inches

    @Column
    val weight: Double? = null, // in pounds

    @Column
    val email: String? = null,

    @Column
    val phone: String? = null,

    @Column
    val emergencyContactName: String? = null,

    @Column
    val emergencyContactPhone: String? = null,

    @Column
    val medicalNotes: String? = null,

    @Column
    val profilePhotoUrl: String? = null,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(unique = true)
    val userId: Long? = null, // Link to User entity for authentication

    @OneToMany(mappedBy = "athlete", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val athleteWorkouts: List<AthleteWorkout> = emptyList(),

    @OneToMany(mappedBy = "athlete", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val performanceMetrics: List<PerformanceMetric> = emptyList(),

    @OneToMany(mappedBy = "athlete", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val assessmentResults: List<AssessmentResult> = emptyList(),

    @OneToMany(mappedBy = "athlete", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val assessmentSchedules: List<AssessmentSchedule> = emptyList()
) {
    val fullName: String
        get() = "$firstName $lastName"
    
    val age: Int
        get() = LocalDate.now().year - dateOfBirth.year
}