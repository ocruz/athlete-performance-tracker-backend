package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "athlete_programs")
data class AthleteProgram(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    @JsonIgnore
    val athlete: Athlete,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    @JsonIgnore
    val program: Program,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    @JsonIgnore
    val assignedBy: User,

    @Column(nullable = false)
    val startDate: LocalDate,

    @Column
    val endDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ProgramStatus = ProgramStatus.ACTIVE,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "athleteProgram", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    val progressEntries: List<ProgramProgress> = emptyList()
)

enum class ProgramStatus {
    ACTIVE,
    COMPLETED,
    PAUSED,
    CANCELLED
}