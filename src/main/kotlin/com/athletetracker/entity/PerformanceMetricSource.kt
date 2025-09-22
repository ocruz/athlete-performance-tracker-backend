package com.athletetracker.entity

/**
 * Enum representing the source of a performance metric.
 * This allows us to track how a metric was generated and provides context for data analysis.
 */
enum class PerformanceMetricSource {
    /**
     * Manually entered by a coach or athlete
     */
    MANUAL,
    
    /**
     * Generated automatically from an assessment result
     */
    ASSESSMENT,
    
    /**
     * Generated automatically from a workout personal record
     */
    WORKOUT_PR
}