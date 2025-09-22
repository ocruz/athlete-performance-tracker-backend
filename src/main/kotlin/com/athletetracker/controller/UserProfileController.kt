package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.service.AthleteService
import com.athletetracker.service.UserProfileService
import com.athletetracker.service.CoachService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = ["http://localhost:3000"])
class UserProfileController(
    private val userProfileService: UserProfileService,
    private val coachService: CoachService,
    private val athleteService: AthleteService
) {

    @GetMapping("/profile")
    fun getUserProfile(authentication: Authentication): ResponseEntity<UserProfileDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val profile = userProfileService.getUserProfile(userId)
        return ResponseEntity.ok(profile)
    }

    @GetMapping("/profile/complete")
    fun getCompleteProfile(authentication: Authentication): ResponseEntity<CompleteProfileDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val user = getUserFromAuthentication(authentication)
        val userProfile = userProfileService.getUserProfile(userId)
        
        return when (user.role.name) {
            "COACH" -> {
                val coach = coachService.getCoachByUserId(userId)
                val userDto = user.toDto()
                val completeProfile = CompleteProfileDto.forCoach(userDto, coach, userProfile)
                ResponseEntity.ok(completeProfile)
            }
            else -> {
                val userDto = user.toDto()
                val athlete = athleteService.getAthleteByUserId(userId)
                val completeProfile = CompleteProfileDto.forAthlete(userDto, athlete, userProfile)
                ResponseEntity.ok(completeProfile)
            }
        }
    }

    @PutMapping("/profile")
    fun updateUserProfile(
        @Valid @RequestBody updateRequest: UpdateUserProfileRequest,
        authentication: Authentication
    ): ResponseEntity<UserProfileDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val updatedProfile = userProfileService.updateUserProfile(userId, updateRequest)
        return ResponseEntity.ok(updatedProfile)
    }

    @PutMapping("/profile/basic-info")
    fun updateUserBasicInfo(
        @Valid @RequestBody updateRequest: UpdateUserBasicInfoRequest,
        authentication: Authentication
    ): ResponseEntity<UserProfileDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val updatedProfile = userProfileService.updateUserBasicInfo(userId, updateRequest)
        return ResponseEntity.ok(updatedProfile)
    }

    @GetMapping("/profile/basic-info")
    fun getUserBasicInfo(authentication: Authentication): ResponseEntity<UpdateUserBasicInfoRequest> {
        val userId = getUserIdFromAuthentication(authentication)
        val basicInfo = userProfileService.getUserBasicInfo(userId)
        return ResponseEntity.ok(basicInfo)
    }

    @PostMapping("/profile/change-password")
    fun changePassword(
        @Valid @RequestBody changePasswordRequest: ChangePasswordRequest,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        val userId = getUserIdFromAuthentication(authentication)
        userProfileService.changePassword(userId, changePasswordRequest)
        return ResponseEntity.ok(mapOf("message" to "Password changed successfully"))
    }

    @PutMapping("/profile/coach")
    fun updateCoachProfile(
        @Valid @RequestBody updateRequest: UpdateCoachRequest,
        authentication: Authentication
    ): ResponseEntity<CoachDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val updatedCoach = coachService.updateCoach(userId, updateRequest)
        return ResponseEntity.ok(updatedCoach)
    }

    @PostMapping("/profile/photo")
    fun uploadProfilePhoto(
        @RequestParam("file") file: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<UserProfileDto> {
        val userId = getUserIdFromAuthentication(authentication)
        val user = getUserFromAuthentication(authentication)
        
        // Basic file validation
        if (file.isEmpty) {
            return ResponseEntity.badRequest().build()
        }
        
        if (!isValidImageFile(file)) {
            return ResponseEntity.badRequest().build()
        }
        
        // For now, we'll just return the filename as the URL
        // In a real implementation, you'd upload to cloud storage and return the actual URL
        val photoUrl = "/uploads/profiles/${userId}_${file.originalFilename}"
        
        // Update photo in both user profile and coach profile (if coach)
        val updatedProfile = userProfileService.updateProfilePhoto(userId, photoUrl)
        if (user.role.name == "COACH") {
            coachService.updateCoachPhoto(userId, photoUrl)
        }
        
        return ResponseEntity.ok(updatedProfile)
    }

    private fun getUserIdFromAuthentication(authentication: Authentication): Long {
        return when (val principal = authentication.principal) {
            is com.athletetracker.entity.User -> principal.id
            else -> throw IllegalStateException("Invalid authentication principal")
        }
    }

    private fun getUserFromAuthentication(authentication: Authentication): com.athletetracker.entity.User {
        return when (val principal = authentication.principal) {
            is com.athletetracker.entity.User -> principal
            else -> throw IllegalStateException("Invalid authentication principal")
        }
    }

    private fun isValidImageFile(file: MultipartFile): Boolean {
        val allowedTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/gif")
        return file.contentType in allowedTypes && file.size <= 5 * 1024 * 1024 // 5MB limit
    }
}