package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.Athlete
import com.athletetracker.entity.Sport
import com.athletetracker.repository.AthleteRepository
import com.athletetracker.repository.PerformanceMetricRepository
import com.athletetracker.repository.WorkoutRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class AthleteService(
    private val athleteRepository: AthleteRepository,
    private val workoutRepository: WorkoutRepository,
    private val performanceMetricRepository: PerformanceMetricRepository
) {
    
    fun getAthleteEntityById(id: Long): Athlete {
        return athleteRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Athlete not found with id: $id")
    }
    
    @Transactional(readOnly = true)
    fun findAthleteByUserId(userId: Long): Athlete? {
        return athleteRepository.findByUserId(userId)
    }
    
    fun createAthlete(request: CreateAthleteRequest): AthleteDto {
        val athlete = Athlete(
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            sport = request.sport,
            position = request.position,
            height = request.height,
            weight = request.weight,
            email = request.email,
            phone = request.phone,
            emergencyContactName = request.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone,
            medicalNotes = request.medicalNotes
        )
        
        val savedAthlete = athleteRepository.save(athlete)
        
        // Send invitation if email is provided
        if (!request.email.isNullOrBlank()) {
            try {
                // Note: This will be handled by a separate service call from the controller
                // to avoid circular dependency issues
                println("📧 Athlete created with email: ${request.email}. Invitation should be sent by controller.")
            } catch (e: Exception) {
                println("⚠️  Failed to send invitation to ${request.email}: ${e.message}")
            }
        }
        
        return mapToResponse(savedAthlete)
    }
    
    /**
     * Link an athlete profile to a user account
     */
    fun linkAthleteToUser(athleteId: Long, userId: Long) {
        val athlete = athleteRepository.findByIdOrNull(athleteId)
            ?: throw IllegalArgumentException("Athlete not found with id: $athleteId")
        
        val updatedAthlete = athlete.copy(
            userId = userId,
            updatedAt = LocalDateTime.now()
        )
        
        athleteRepository.save(updatedAthlete)
    }
    
    fun updateAthlete(id: Long, request: UpdateAthleteRequest): AthleteDto {
        val athlete = athleteRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Athlete not found with id: $id")
        
        val updatedAthlete = athlete.copy(
            firstName = request.firstName ?: athlete.firstName,
            lastName = request.lastName ?: athlete.lastName,
            dateOfBirth = request.dateOfBirth ?: athlete.dateOfBirth,
            sport = request.sport ?: athlete.sport,
            position = request.position ?: athlete.position,
            height = request.height ?: athlete.height,
            weight = request.weight ?: athlete.weight,
            email = request.email ?: athlete.email,
            phone = request.phone ?: athlete.phone,
            emergencyContactName = request.emergencyContactName ?: athlete.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone ?: athlete.emergencyContactPhone,
            medicalNotes = request.medicalNotes ?: athlete.medicalNotes,
            isActive = request.isActive ?: athlete.isActive,
            updatedAt = LocalDateTime.now()
        )
        
        val savedAthlete = athleteRepository.save(updatedAthlete)
        return mapToResponse(savedAthlete)
    }
    
    @Transactional(readOnly = true)
    fun getAthleteById(id: Long): AthleteDto {
        val athlete = athleteRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Athlete not found with id: $id")
        return mapToResponse(athlete)
    }

    @Transactional(readOnly = true)
    fun getAthleteByUserId(id: Long): AthleteDto {
        val athlete = athleteRepository.findByUserId(id)
            ?: throw IllegalArgumentException("Athlete not found with id: $id")
        return mapToResponse(athlete)
    }
    
    @Transactional(readOnly = true)
    fun getAllAthletes(): List<AthleteDto> {
        return athleteRepository.findByIsActiveTrue()
            .map { mapToResponse(it) }
    }
    
    @Transactional(readOnly = true)
    fun getAthletesBySport(sport: Sport): List<AthleteDto> {
        return athleteRepository.findBySportAndIsActiveTrue(sport)
            .map { mapToResponse(it) }
    }
    
    @Transactional(readOnly = true)
    fun searchAthletes(searchRequest: AthleteSearchRequest): List<AthleteDto> {
        var athletes = if (searchRequest.sport != null) {
            athleteRepository.findBySportAndIsActiveTrue(searchRequest.sport)
        } else {
            athleteRepository.findByIsActiveTrue()
        }
        
        // Apply name search filter
        if (!searchRequest.query.isNullOrBlank()) {
            athletes = athletes.filter { athlete ->
                athlete.fullName.contains(searchRequest.query, ignoreCase = true)
            }
        }
        
        // Apply position filter
        if (!searchRequest.position.isNullOrBlank()) {
            athletes = athletes.filter { athlete ->
                athlete.position?.contains(searchRequest.position, ignoreCase = true) == true
            }
        }
        
        // Apply age filters
        if (searchRequest.minAge != null || searchRequest.maxAge != null) {
            athletes = athletes.filter { athlete ->
                val age = athlete.age
                (searchRequest.minAge == null || age >= searchRequest.minAge) &&
                (searchRequest.maxAge == null || age <= searchRequest.maxAge)
            }
        }
        
        return athletes.map { mapToResponse(it) }
    }
    
    fun deleteAthlete(id: Long) {
        val athlete = athleteRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Athlete not found with id: $id")
        
        // Soft delete
        val deactivatedAthlete = athlete.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
        athleteRepository.save(deactivatedAthlete)
    }
    
    fun updateAthleteByUserId(userId: Long, request: UpdateAthleteRequest): AthleteDto {
        val athlete = athleteRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("Athlete not found for user: $userId")
        
        val updatedAthlete = athlete.copy(
            firstName = request.firstName ?: athlete.firstName,
            lastName = request.lastName ?: athlete.lastName,
            dateOfBirth = request.dateOfBirth ?: athlete.dateOfBirth,
            sport = request.sport ?: athlete.sport,
            position = request.position ?: athlete.position,
            height = request.height ?: athlete.height,
            weight = request.weight ?: athlete.weight,
            email = request.email ?: athlete.email,
            phone = request.phone ?: athlete.phone,
            emergencyContactName = request.emergencyContactName ?: athlete.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone ?: athlete.emergencyContactPhone,
            medicalNotes = request.medicalNotes ?: athlete.medicalNotes,
            isActive = request.isActive ?: athlete.isActive,
            updatedAt = LocalDateTime.now()
        )
        
        val savedAthlete = athleteRepository.save(updatedAthlete)
        return mapToResponse(savedAthlete)
    }
    
    fun updateAthletePhoto(userId: Long, photoUrl: String): AthleteDto {
        val athlete = athleteRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("Athlete not found for user: $userId")

        val updatedAthlete = athlete.copy(
            profilePhotoUrl = photoUrl,
            updatedAt = LocalDateTime.now()
        )

        val savedAthlete = athleteRepository.save(updatedAthlete)
        return mapToResponse(savedAthlete)
    }
    
    @Transactional(readOnly = true)
    fun getAthleteResponseByUserId(userId: Long): AthleteDto {
        val athlete = athleteRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("Athlete not found for user: $userId")
        return mapToResponse(athlete)
    }
    
    @Transactional(readOnly = true)
    fun getAthleteStats(): Map<String, Any> {
        val totalAthletes = athleteRepository.countByIsActiveTrue()
        val athletesBySport = Sport.values().associateWith { sport ->
            athleteRepository.countBySportAndIsActiveTrue(sport)
        }
        
        return mapOf(
            "totalAthletes" to totalAthletes,
            "athletesBySport" to athletesBySport
        )
    }

    private fun mapToResponse(athlete: Athlete): AthleteDto {
        val totalWorkouts = workoutRepository.countByAthlete(athlete)
        val recentMetrics = performanceMetricRepository.findByAthleteOrderByTestDateDesc(athlete)
            .take(5)
            .map { metric ->
                PerformanceMetricResponse(
                    id = metric.id,
                    metricType = metric.metricType,
                    metricValue = metric.metricValue,
                    unit = metric.unit,
                    testDate = metric.testDate,
                    notes = metric.notes,
                    recordedBy = metric.recordedBy?.fullName
                )
            }
        
        return AthleteDto(
            id = athlete.id,
            firstName = athlete.firstName,
            lastName = athlete.lastName,
            fullName = athlete.fullName,
            dateOfBirth = athlete.dateOfBirth,
            age = athlete.age,
            sport = athlete.sport,
            position = athlete.position,
            height = athlete.height,
            weight = athlete.weight,
            email = athlete.email,
            phone = athlete.phone,
            emergencyContactName = athlete.emergencyContactName,
            emergencyContactPhone = athlete.emergencyContactPhone,
            medicalNotes = athlete.medicalNotes,
            profilePhotoUrl = athlete.profilePhotoUrl,
            isActive = athlete.isActive,
            totalWorkouts = totalWorkouts,
            recentMetrics = recentMetrics
        )
    }
}