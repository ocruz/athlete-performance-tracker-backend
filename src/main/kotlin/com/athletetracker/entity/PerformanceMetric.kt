package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "performance_metrics")
data class PerformanceMetric(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    val athlete: Athlete,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val metricType: MetricType,

    @Column(nullable = false)
    val metricValue: Double,

    @Column
    val unit: String? = null,

    @Column(nullable = false)
    val testDate: LocalDateTime,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    val recordedBy: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val sourceType: PerformanceMetricSource = PerformanceMetricSource.MANUAL,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_assessment_result_id")
    @JsonIgnore
    val sourceAssessmentResult: AssessmentResult? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_workout_exercise_id")
    @JsonIgnore
    val sourceWorkoutExercise: AthleteWorkoutExercise? = null,

    @Column(nullable = false)
    val isPersonalRecord: Boolean = false,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class MetricType {
    // Strength Metrics
    BENCH_PRESS_MAX,
    SQUAT_MAX,
    DEADLIFT_MAX,
    OVERHEAD_PRESS_MAX,
    
    // Power Metrics
    VERTICAL_JUMP,
    BROAD_JUMP,
    MEDICINE_BALL_THROW,
    
    // Speed/Agility Metrics
    FORTY_YARD_DASH,
    TWENTY_YARD_DASH,
    TEN_YARD_DASH,
    SHUTTLE_RUN,
    THREE_CONE_DRILL,
    
    // Endurance Metrics
    MILE_TIME,
    HALF_MILE_TIME,
    VO2_MAX,
    RESTING_HEART_RATE,
    
    // Body Composition
    BODY_WEIGHT,
    BODY_FAT_PERCENTAGE,
    MUSCLE_MASS,
    
    // Sport-Specific Metrics
    BATTING_VELOCITY,
    PITCH_VELOCITY,
    SERVE_VELOCITY,
    SHOT_VELOCITY,
    
    // Flexibility/Mobility
    SIT_AND_REACH,
    SHOULDER_FLEXIBILITY,
    HIP_FLEXIBILITY,
    
    // Custom Metrics
    CUSTOM
}