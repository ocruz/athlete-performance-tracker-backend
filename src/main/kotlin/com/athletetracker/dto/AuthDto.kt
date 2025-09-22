package com.athletetracker.dto

import com.athletetracker.entity.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class LoginRequest(
    @field:Email(message = "Please provide a valid email address")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String
)

data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val user: UserDto,
    val expiresIn: Long,
    val defaultRoute: String
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

data class RefreshTokenResponse(
    val token: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val role: UserRole,
    val isActive: Boolean,
    val defaultRoute: String?,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,
    
    @field:NotBlank(message = "New password is required")
    @field:Size(min = 6, message = "New password must be at least 6 characters")
    val newPassword: String,
    
    @field:NotBlank(message = "Confirm password is required")
    @field:Size(min = 6, message = "Confirm password must be at least 6 characters")
    val confirmPassword: String
)

data class UpdateProfileRequest(
    @field:NotBlank(message = "First name is required")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    val lastName: String
)

// Extension function to convert User entity to UserDto
fun com.athletetracker.entity.User.toDto(): UserDto {
    return UserDto(
        id = this.id,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        fullName = this.fullName,
        role = this.role,
        isActive = this.isActive,
        defaultRoute = this.defaultRoute,
        lastLoginAt = this.lastLoginAt,
        createdAt = this.createdAt
    )
}