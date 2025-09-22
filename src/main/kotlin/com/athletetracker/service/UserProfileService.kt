package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.UserProfile
import com.athletetracker.entity.UserRole
import com.athletetracker.repository.UserProfileRepository
import com.athletetracker.repository.UserRepository
import com.athletetracker.repository.AthleteRepository
import com.athletetracker.repository.CoachRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class UserProfileService(
    private val userProfileRepository: UserProfileRepository,
    private val userRepository: UserRepository,
    private val athleteRepository: AthleteRepository,
    private val coachRepository: CoachRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional(readOnly = true)
    fun getUserProfile(userId: Long): UserProfileDto {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }
        
        val userProfile = userProfileRepository.findByUserId(userId)
            .orElse(
                // Create default profile if none exists
                UserProfile(user = user)
            )
        
        return userProfile.toDto()
    }


    fun updateUserProfile(userId: Long, updateRequest: UpdateUserProfileRequest): UserProfileDto {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        val existingProfile = userProfileRepository.findByUserId(userId)
            .orElse(UserProfile(user = user))

        val updatedProfile = existingProfile.copy(
            phone = updateRequest.phone?.takeIf { it.isNotBlank() } ?: existingProfile.phone,
            notificationPreferences = updateRequest.notificationPreferences?.takeIf { it.isNotBlank() } ?: existingProfile.notificationPreferences,
            preferredLanguage = updateRequest.preferredLanguage?.takeIf { it.isNotBlank() } ?: existingProfile.preferredLanguage ?: "en",
            timezone = updateRequest.timezone?.takeIf { it.isNotBlank() } ?: existingProfile.timezone ?: "UTC",
            profileVisibility = updateRequest.profileVisibility?.takeIf { it.isNotBlank() } ?: existingProfile.profileVisibility ?: "PUBLIC",
            updatedAt = LocalDateTime.now()
        )

        val savedProfile = userProfileRepository.save(updatedProfile)
        return savedProfile.toDto()
    }

    fun updateUserBasicInfo(userId: Long, updateRequest: UpdateUserBasicInfoRequest): UserProfileDto {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        // Check if email is already taken by another user
        userRepository.findByEmail(updateRequest.email)?.let { existingUser ->
            if (existingUser.id != userId) {
                throw IllegalArgumentException("Email is already in use by another user")
            }
        }

        val updatedUser = user.copy(
            firstName = updateRequest.firstName,
            lastName = updateRequest.lastName,
            email = updateRequest.email,
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)

        // Return updated profile
        return getUserProfile(userId)
    }

    fun changePassword(userId: Long, changePasswordRequest: ChangePasswordRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        // Validate current password
        if (!passwordEncoder.matches(changePasswordRequest.currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        // Validate new password confirmation
        if (changePasswordRequest.newPassword != changePasswordRequest.confirmPassword) {
            throw IllegalArgumentException("New password and confirmation do not match")
        }

        // Update password
        val updatedUser = user.copy(
            password = passwordEncoder.encode(changePasswordRequest.newPassword),
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)
    }

    fun updateProfilePhoto(userId: Long, photoUrl: String): UserProfileDto {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        val existingProfile = userProfileRepository.findByUserId(userId)
            .orElse(UserProfile(user = user))

        val updatedProfile = existingProfile.copy(
            profilePhotoUrl = photoUrl,
            updatedAt = LocalDateTime.now()
        )

        val savedProfile = userProfileRepository.save(updatedProfile)
        return savedProfile.toDto()
    }

    @Transactional(readOnly = true)
    fun getUserBasicInfo(userId: Long): UpdateUserBasicInfoRequest {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        return UpdateUserBasicInfoRequest(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email
        )
    }
}