package com.athletetracker.dto

import com.athletetracker.entity.Coach
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class CoachDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val officeLocation: String?,
    val yearsExperience: Int?,
    val certifications: String?,
    val specializations: String?,
    val coachingPhilosophy: String?,
    val preferredSports: String?,
    val preferredContactMethod: String?,
    val availabilityHours: String?,
    val bio: String?,
    val profilePhotoUrl: String?,
    val isActive: Boolean,
    val userId: Long?
)

data class UpdateCoachRequest(
    @field:Email(message = "Email must be valid")
    @field:Size(max = 255, message = "Email cannot exceed 255 characters")
    val email: String? = null,

    @field:Size(max = 20, message = "Phone number cannot exceed 20 characters")
    val phone: String? = null,

    @field:Size(max = 200, message = "Office location cannot exceed 200 characters")
    val officeLocation: String? = null,

    @field:Min(value = 0, message = "Years of experience cannot be negative")
    @field:Max(value = 50, message = "Years of experience cannot exceed 50")
    val yearsExperience: Int? = null,

    @field:Size(max = 1000, message = "Certifications cannot exceed 1000 characters")
    val certifications: String? = null,

    @field:Size(max = 500, message = "Specializations cannot exceed 500 characters")
    val specializations: String? = null,

    @field:Size(max = 2000, message = "Coaching philosophy cannot exceed 2000 characters")
    val coachingPhilosophy: String? = null,

    @field:Size(max = 500, message = "Preferred sports cannot exceed 500 characters")
    val preferredSports: String? = null,

    @field:Size(max = 50, message = "Preferred contact method cannot exceed 50 characters")
    val preferredContactMethod: String? = null,

    @field:Size(max = 200, message = "Availability hours cannot exceed 200 characters")
    val availabilityHours: String? = null,

    @field:Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    val bio: String? = null
)

data class CreateCoachRequest(
    @field:Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    val firstName: String,

    @field:Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    val lastName: String,

    @field:Email(message = "Email must be valid")
    val email: String? = null,

    @field:Size(max = 20, message = "Phone number cannot exceed 20 characters")
    val phone: String? = null,

    val userId: Long
)

fun Coach.toDto() = CoachDto(
    id = id,
    firstName = firstName,
    lastName = lastName,
    fullName = fullName,
    email = email,
    phone = phone,
    officeLocation = officeLocation,
    yearsExperience = yearsExperience,
    certifications = certifications,
    specializations = specializations,
    coachingPhilosophy = coachingPhilosophy,
    preferredSports = preferredSports,
    preferredContactMethod = preferredContactMethod,
    availabilityHours = availabilityHours,
    bio = bio,
    profilePhotoUrl = profilePhotoUrl,
    isActive = isActive,
    userId = userId
)