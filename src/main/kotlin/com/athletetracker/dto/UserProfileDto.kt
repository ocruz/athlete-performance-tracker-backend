package com.athletetracker.dto

import com.athletetracker.entity.UserProfile
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UserProfileDto(
    val id: Long,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val profilePhotoUrl: String? = null,
    val notificationPreferences: String? = null,
    val preferredLanguage: String? = null,
    val timezone: String? = null,
    val profileVisibility: String? = null
)

data class UpdateUserProfileRequest(
    @field:Size(max = 20, message = "Phone number cannot exceed 20 characters")
    val phone: String? = null,

    @field:Size(max = 1000, message = "Notification preferences cannot exceed 1000 characters")
    val notificationPreferences: String? = null,

    @field:Size(max = 10, message = "Language code cannot exceed 10 characters")
    val preferredLanguage: String? = null,

    @field:Size(max = 50, message = "Timezone cannot exceed 50 characters")
    val timezone: String? = null,

    @field:Size(max = 20, message = "Profile visibility cannot exceed 20 characters")
    val profileVisibility: String? = null
)

data class UpdateUserBasicInfoRequest(
    @field:Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    val firstName: String,

    @field:Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    val lastName: String,

    @field:Email(message = "Email must be valid")
    @field:Size(max = 100, message = "Email cannot exceed 100 characters")
    val email: String
)


fun UserProfile.toDto() = UserProfileDto(
    id = id,
    userId = user.id,
    firstName = user.firstName,
    lastName = user.lastName,
    email = user.email,
    phone = phone,
    profilePhotoUrl = profilePhotoUrl,
    notificationPreferences = notificationPreferences,
    preferredLanguage = preferredLanguage,
    timezone = timezone,
    profileVisibility = profileVisibility
)