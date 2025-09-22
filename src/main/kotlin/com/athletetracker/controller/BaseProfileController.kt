package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.service.UserProfileService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Abstract base controller containing shared profile functionality for all user types.
 * Provides common operations like photo upload, basic info management, universal preferences,
 * and authentication helpers that can be inherited by role-specific controllers.
 */
abstract class BaseProfileController(
    protected val userProfileService: UserProfileService
) {

    /**
     * Get basic user information for editing
     */
    @GetMapping("/profile/basic-info")
    fun getUserBasicInfo(authentication: Authentication): ResponseEntity<UpdateUserBasicInfoRequest> {
        val userId = getUserIdFromAuthentication(authentication)
        val basicInfo = userProfileService.getUserBasicInfo(userId)
        return ResponseEntity.ok(basicInfo)
    }

    /**
     * Update basic user information (name, email)
     */
    @PutMapping("/profile/basic-info")
    fun updateUserBasicInfo(
        @Valid @RequestBody updateRequest: UpdateUserBasicInfoRequest,
        authentication: Authentication
    ): ResponseEntity<UserProfileDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val updatedProfile = userProfileService.updateUserBasicInfo(userId, updateRequest)
        return ResponseEntity.ok(updatedProfile)
    }

    /**
     * Update universal user preferences (phone, notifications, language, timezone, visibility)
     */
    @PutMapping("/profile/preferences")
    fun updateUserPreferences(
        @Valid @RequestBody updateRequest: UpdateUserProfileRequest,
        authentication: Authentication
    ): ResponseEntity<UserProfileDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val updatedProfile = userProfileService.updateUserProfile(userId, updateRequest)
        return ResponseEntity.ok(updatedProfile)
    }

    /**
     * Change user password
     */
    @PostMapping("/profile/change-password")
    fun changePassword(
        @Valid @RequestBody changePasswordRequest: ChangePasswordRequest,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        val userId = getUserIdFromAuthentication(authentication)
        userProfileService.changePassword(userId, changePasswordRequest)
        return ResponseEntity.ok(mapOf("message" to "Password changed successfully"))
    }

    /**
     * Upload profile photo - to be implemented by subclasses for role-specific behavior
     */
    @PostMapping("/profile/photo")
    open fun uploadProfilePhoto(
        @RequestParam("file") file: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<*> {
        val userId = getUserIdFromAuthentication(authentication)
        
        // Basic file validation
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(mapOf("error" to "File is required"))
        }
        
        if (!isValidImageFile(file)) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid image file"))
        }
        
        // For now, we'll just return the filename as the URL
        // In a real implementation, you'd upload to cloud storage and return the actual URL
        val photoUrl = "/uploads/profiles/${userId}_${file.originalFilename}"
        
        // Update photo in user profile
        val updatedProfile = userProfileService.updateProfilePhoto(userId, photoUrl)
        return ResponseEntity.ok(updatedProfile)
    }

    /**
     * Exception handlers
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to "An unexpected error occurred"))
    }

    /**
     * Helper methods for authentication
     */
    protected fun getUserIdFromAuthentication(authentication: Authentication): Long {
        return when (val principal = authentication.principal) {
            is com.athletetracker.entity.User -> principal.id
            else -> throw IllegalStateException("Invalid authentication principal")
        }
    }

    protected fun getUserFromAuthentication(authentication: Authentication): com.athletetracker.entity.User {
        return when (val principal = authentication.principal) {
            is com.athletetracker.entity.User -> principal
            else -> throw IllegalStateException("Invalid authentication principal")
        }
    }

    /**
     * File validation helper
     */
    protected fun isValidImageFile(file: MultipartFile): Boolean {
        val allowedTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/gif")
        return file.contentType in allowedTypes && file.size <= 5 * 1024 * 1024 // 5MB limit
    }
}