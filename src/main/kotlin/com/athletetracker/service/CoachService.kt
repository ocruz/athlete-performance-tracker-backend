package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.Coach
import com.athletetracker.repository.CoachRepository
import com.athletetracker.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class CoachService(
    private val coachRepository: CoachRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getCoachByUserId(userId: Long): CoachDto? {
        return coachRepository.findByUserId(userId)
            .map { it.toDto() }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    fun getCoachById(id: Long): CoachDto {
        val coach = coachRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Coach not found with id: $id") }
        return coach.toDto()
    }

    fun createCoach(request: CreateCoachProfileRequest): CoachDto {
        val user = userRepository.findById(request.userId)
            .orElseThrow { IllegalArgumentException("User not found with id: ${request.userId}") }

        if (coachRepository.existsByUserId(request.userId)) {
            throw IllegalArgumentException("Coach profile already exists for user: ${request.userId}")
        }

        val coach = Coach(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            phone = request.phone,
            userId = request.userId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedCoach = coachRepository.save(coach)
        return savedCoach.toDto()
    }

    fun updateCoach(userId: Long, request: UpdateCoachRequest): CoachDto {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        val existingCoach = coachRepository.findByUserId(userId)
            .orElse(
                Coach(
                    firstName = user.firstName,
                    lastName = user.lastName,
                    userId = userId
                )
            )

        val updatedCoach = existingCoach.copy(
            email = request.email ?: existingCoach.email,
            phone = request.phone ?: existingCoach.phone,
            officeLocation = request.officeLocation ?: existingCoach.officeLocation,
            yearsExperience = request.yearsExperience ?: existingCoach.yearsExperience,
            certifications = request.certifications ?: existingCoach.certifications,
            specializations = request.specializations ?: existingCoach.specializations,
            coachingPhilosophy = request.coachingPhilosophy ?: existingCoach.coachingPhilosophy,
            preferredSports = request.preferredSports ?: existingCoach.preferredSports,
            preferredContactMethod = request.preferredContactMethod ?: existingCoach.preferredContactMethod,
            availabilityHours = request.availabilityHours ?: existingCoach.availabilityHours,
            bio = request.bio ?: existingCoach.bio,
            updatedAt = LocalDateTime.now()
        )

        val savedCoach = coachRepository.save(updatedCoach)
        return savedCoach.toDto()
    }

    fun updateCoachPhoto(userId: Long, photoUrl: String): CoachDto {
        val existingCoach = coachRepository.findByUserId(userId)
            .orElseThrow { IllegalArgumentException("Coach not found for user: $userId") }

        val updatedCoach = existingCoach.copy(
            profilePhotoUrl = photoUrl,
            updatedAt = LocalDateTime.now()
        )

        val savedCoach = coachRepository.save(updatedCoach)
        return savedCoach.toDto()
    }

    @Transactional(readOnly = true)
    fun getAllActiveCoaches(): List<CoachDto> {
        return coachRepository.findAllByIsActive(true)
            .map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun searchCoaches(query: String): List<CoachDto> {
        return coachRepository.searchCoaches(query)
            .map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun findCoachesBySpecialization(specialization: String): List<CoachDto> {
        return coachRepository.findBySpecialization(specialization)
            .map { it.toDto() }
    }
}