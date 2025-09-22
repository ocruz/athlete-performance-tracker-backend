package com.athletetracker.dto

import com.athletetracker.entity.Sport
import jakarta.validation.constraints.*
import java.time.LocalDate

data class CreateAthleteRequest(
    @field:NotBlank(message = "First name is required")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    val lastName: String,
    
    @field:NotNull(message = "Date of birth is required")
    @field:Past(message = "Date of birth must be in the past")
    val dateOfBirth: LocalDate,
    
    @field:NotNull(message = "Sport is required")
    val sport: Sport,
    
    val position: String? = null,
    
    @field:Positive(message = "Height must be positive")
    val height: Double? = null,
    
    @field:Positive(message = "Weight must be positive")
    val weight: Double? = null,
    
    @field:Email(message = "Email must be valid")
    val email: String? = null,
    
    val phone: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val medicalNotes: String? = null
)

data class UpdateAthleteRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val dateOfBirth: LocalDate? = null,
    val sport: Sport? = null,
    val position: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val email: String? = null,
    val phone: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val medicalNotes: String? = null,
    val isActive: Boolean? = null
)

data class AthleteDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val dateOfBirth: LocalDate,
    val age: Int,
    val sport: Sport,
    val position: String?,
    val height: Double?,
    val weight: Double?,
    val email: String?,
    val phone: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val medicalNotes: String?,
    val profilePhotoUrl: String?,
    val isActive: Boolean,
    val totalWorkouts: Long = 0,
    val recentMetrics: List<PerformanceMetricResponse> = emptyList()
)

data class AthleteSearchRequest(
    val query: String? = null,
    val sport: Sport? = null,
    val position: String? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val isActive: Boolean = true
)