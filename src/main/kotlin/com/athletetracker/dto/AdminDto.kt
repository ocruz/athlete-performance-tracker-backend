package com.athletetracker.dto

import com.athletetracker.entity.Sport
import com.athletetracker.entity.UserRole
import java.time.LocalDate
import java.time.LocalDateTime

// User Management DTOs

data class CreateCoachRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val specialization: String? = null,
    val certifications: List<String>? = null
)

data class CreateAthleteByAdminRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val sport: Sport,
    val dateOfBirth: LocalDate,
    val position: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val phone: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val medicalNotes: String? = null
)

data class AdminUpdateUserProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val isActive: Boolean? = null
)

data class AdminChangePasswordRequest(
    val newPassword: String
)

data class AdminUserResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null,
    // Additional info based on role
    val athleteInfo: AthleteInfoDto? = null,
    val coachInfo: CoachInfoDto? = null
)

data class AthleteInfoDto(
    val sport: Sport,
    val dateOfBirth: LocalDate,
    val position: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val phone: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val medicalNotes: String? = null
)

data class CoachInfoDto(
    val specialization: String? = null,
    val certifications: List<String>? = null,
    val totalAthletes: Int = 0,
    val activePrograms: Int = 0
)

data class AdminDashboardStatsResponse(
    val totalUsers: Int,
    val totalCoaches: Int,
    val totalAthletes: Int,
    val activeUsers: Int,
    val inactiveUsers: Int,
    val recentRegistrations: Int, // Last 30 days
    val totalPrograms: Int,
    val activePrograms: Int,
    val usersByRole: Map<UserRole, Int>,
    val usersBySport: Map<Sport, Int>,
    val recentActivity: List<RecentActivityDto>
)

data class RecentActivityDto(
    val id: Long,
    val type: ActivityType,
    val description: String,
    val userId: Long,
    val userName: String,
    val timestamp: LocalDateTime
)

enum class ActivityType {
    USER_CREATED,
    USER_UPDATED,
    USER_ACTIVATED,
    USER_DEACTIVATED,
    PASSWORD_CHANGED,
    LOGIN
}

data class UserListRequest(
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC",
    val role: UserRole? = null,
    val isActive: Boolean? = null,
    val sport: Sport? = null,
    val searchQuery: String? = null
)

data class UserListResponse(
    val users: List<AdminUserResponse>,
    val totalElements: Int,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

data class BulkUserActionRequest(
    val userIds: List<Long>,
    val action: BulkAction
)

enum class BulkAction {
    ACTIVATE,
    DEACTIVATE,
    DELETE
}

data class UserCreationResponse(
    val user: AdminUserResponse,
    val temporaryPassword: String? = null,
    val message: String
)