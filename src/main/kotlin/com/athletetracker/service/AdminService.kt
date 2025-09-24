package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.*
import com.athletetracker.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.security.SecureRandom

@Service
@Transactional
class AdminService(
    private val userRepository: UserRepository,
    private val athleteRepository: AthleteRepository,
    private val programRepository: ProgramRepository,
    private val programWorkoutRepository: ProgramWorkoutRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun createCoach(request: CreateCoachRequest): UserCreationResponse {
        // Check if email already exists
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists: ${request.email}")
        }

        // Create user entity
        val user = User(
            id = 0, // Will be generated
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            role = UserRole.COACH,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(user)

        return UserCreationResponse(
            user = convertToAdminUserResponse(savedUser),
            message = "Coach created successfully"
        )
    }

    fun createAthlete(request: CreateAthleteByAdminRequest): UserCreationResponse {
        // Check if email already exists
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists: ${request.email}")
        }

        // Create user entity first
        val user = User(
            id = 0, // Will be generated
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            role = UserRole.ATHLETE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(user)

        // Create athlete entity
        val athlete = Athlete(
            id = 0, // Will be generated
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
            medicalNotes = request.medicalNotes,
            profilePhotoUrl = null,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        athleteRepository.save(athlete)

        return UserCreationResponse(
            user = convertToAdminUserResponse(savedUser, athlete),
            message = "Athlete created successfully"
        )
    }

    fun getAllUsers(request: UserListRequest): UserListResponse {
        val sort = Sort.by(
            if (request.sortDirection.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC,
            request.sortBy
        )
        
        val pageable = PageRequest.of(request.page, request.size, sort)
        
        // Build query criteria based on filters
        val users = when {
            request.searchQuery != null -> {
                userRepository.findBySearchCriteria(
                    searchQuery = request.searchQuery,
                    role = request.role,
                    isActive = request.isActive,
                    pageable = pageable
                )
            }
            request.role != null -> {
                userRepository.findByRoleAndIsActiveOrderByCreatedAtDesc(
                    role = request.role,
                    isActive = request.isActive ?: true,
                    pageable = pageable
                )
            }
            request.isActive != null -> {
                userRepository.findByIsActiveOrderByCreatedAtDesc(
                    isActive = request.isActive,
                    pageable = pageable
                )
            }
            else -> {
                userRepository.findAll(pageable)
            }
        }

        val userResponses = users.content.map { user ->
            val athlete = if (user.role == UserRole.ATHLETE) {
                athleteRepository.findByEmail(user.email)
            } else null
            convertToAdminUserResponse(user, athlete)
        }

        return UserListResponse(
            users = userResponses,
            totalElements = users.totalElements.toInt(),
            totalPages = users.totalPages,
            currentPage = users.number,
            pageSize = users.size
        )
    }

    fun getUserById(userId: Long): AdminUserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }
        
        val athlete = if (user.role == UserRole.ATHLETE) {
            athleteRepository.findByEmail(user.email)
        } else null

        return convertToAdminUserResponse(user, athlete)
    }

    fun updateUserProfile(userId: Long, request: AdminUpdateUserProfileRequest): AdminUserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        // Check if email is being changed and if it already exists
        if (request.email != null && request.email != user.email) {
            if (userRepository.existsByEmail(request.email)) {
                throw IllegalArgumentException("Email already exists: ${request.email}")
            }
        }

        val updatedUser = user.copy(
            firstName = request.firstName ?: user.firstName,
            lastName = request.lastName ?: user.lastName,
            email = request.email ?: user.email,
            isActive = request.isActive ?: user.isActive,
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(updatedUser)

        // Update athlete info if user is an athlete and email changed
        if (savedUser.role == UserRole.ATHLETE && request.email != null) {
            athleteRepository.findByEmail(user.email)?.let { athlete ->
                val updatedAthlete = athlete.copy(
                    firstName = savedUser.firstName,
                    lastName = savedUser.lastName,
                    email = savedUser.email,
                    updatedAt = LocalDateTime.now()
                )
                athleteRepository.save(updatedAthlete)
            }
        }

        val athlete = if (savedUser.role == UserRole.ATHLETE) {
            athleteRepository.findByEmail(savedUser.email)
        } else null

        return convertToAdminUserResponse(savedUser, athlete)
    }

    fun changeUserPassword(userId: Long, request: AdminChangePasswordRequest): String {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        if (request.newPassword.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters long")
        }

        val updatedUser = user.copy(
            password = passwordEncoder.encode(request.newPassword),
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)
        return "Password updated successfully"
    }

    fun toggleUserStatus(userId: Long): AdminUserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        val updatedUser = user.copy(
            isActive = !user.isActive,
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(updatedUser)

        // Update athlete status if user is an athlete
        if (savedUser.role == UserRole.ATHLETE) {
            athleteRepository.findByEmail(savedUser.email)?.let { athlete ->
                val updatedAthlete = athlete.copy(
                    isActive = savedUser.isActive,
                    updatedAt = LocalDateTime.now()
                )
                athleteRepository.save(updatedAthlete)
            }
        }

        val athlete = if (savedUser.role == UserRole.ATHLETE) {
            athleteRepository.findByEmail(savedUser.email)
        } else null

        return convertToAdminUserResponse(savedUser, athlete)
    }

    fun getDashboardStats(): AdminDashboardStatsResponse {
        val totalUsers = userRepository.count().toInt()
        val totalCoaches = userRepository.countByRole(UserRole.COACH).toInt()
        val totalAthletes = userRepository.countByRole(UserRole.ATHLETE).toInt()
        val activeUsers = userRepository.countByIsActive(true).toInt()
        val inactiveUsers = userRepository.countByIsActive(false).toInt()
        
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
        val recentRegistrations = userRepository.countByCreatedAtAfter(thirtyDaysAgo).toInt()
        
        val totalPrograms = programRepository.count().toInt()
        val activePrograms = programWorkoutRepository.countByIsActive(true).toInt()

        val usersByRole = mapOf(
            UserRole.ADMIN to userRepository.countByRole(UserRole.ADMIN).toInt(),
            UserRole.COACH to totalCoaches,
            UserRole.ATHLETE to totalAthletes
        )

        val usersBySport = Sport.entries.associateWith { sport ->
            athleteRepository.countBySport(sport).toInt()
        }

        val recentActivity = generateRecentActivity()

        return AdminDashboardStatsResponse(
            totalUsers = totalUsers,
            totalCoaches = totalCoaches,
            totalAthletes = totalAthletes,
            activeUsers = activeUsers,
            inactiveUsers = inactiveUsers,
            recentRegistrations = recentRegistrations,
            totalPrograms = totalPrograms,
            activePrograms = activePrograms,
            usersByRole = usersByRole,
            usersBySport = usersBySport,
            recentActivity = recentActivity
        )
    }

    fun performBulkAction(request: BulkUserActionRequest): String {
        val users = userRepository.findAllById(request.userIds)
        
        if (users.size != request.userIds.size) {
            throw IllegalArgumentException("Some users not found")
        }

        when (request.action) {
            BulkAction.ACTIVATE -> {
                users.forEach { user ->
                    val updatedUser = user.copy(isActive = true, updatedAt = LocalDateTime.now())
                    userRepository.save(updatedUser)
                }
                return "${users.size} users activated"
            }
            BulkAction.DEACTIVATE -> {
                users.forEach { user ->
                    val updatedUser = user.copy(isActive = false, updatedAt = LocalDateTime.now())
                    userRepository.save(updatedUser)
                }
                return "${users.size} users deactivated"
            }
            BulkAction.DELETE -> {
                // Soft delete - mark as inactive
                users.forEach { user ->
                    val updatedUser = user.copy(isActive = false, updatedAt = LocalDateTime.now())
                    userRepository.save(updatedUser)
                }
                return "${users.size} users soft deleted"
            }
        }
    }

    private fun convertToAdminUserResponse(user: User, athlete: Athlete? = null): AdminUserResponse {
        val athleteInfo = athlete?.let { 
            AthleteInfoDto(
                sport = it.sport,
                dateOfBirth = it.dateOfBirth,
                position = it.position,
                height = it.height,
                weight = it.weight,
                phone = it.phone,
                emergencyContactName = it.emergencyContactName,
                emergencyContactPhone = it.emergencyContactPhone,
                medicalNotes = it.medicalNotes
            )
        }

        val coachInfo = if (user.role == UserRole.COACH) {
            // Calculate coach statistics
            val totalAthletes = athleteRepository.count().toInt() // In a real implementation, this would be coach-specific
            val activePrograms = programRepository.countByCreatedBy(user)
            
            CoachInfoDto(
                specialization = null, // Would be stored in a coach profile table
                certifications = emptyList(), // Would be stored in a coach profile table
                totalAthletes = totalAthletes,
                activePrograms = activePrograms
            )
        } else null

        return AdminUserResponse(
            id = user.id,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            role = user.role,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            lastLoginAt = null, // Would need login tracking
            athleteInfo = athleteInfo,
            coachInfo = coachInfo
        )
    }

    private fun generateRecentActivity(): List<RecentActivityDto> {
        // In a real implementation, this would query an activity log table
        // For now, return mock recent activity based on recent user updates
        val recentUsers = userRepository.findTop10ByOrderByUpdatedAtDesc()
        
        return recentUsers.mapIndexed { index, user ->
            RecentActivityDto(
                id = index.toLong() + 1,
                type = ActivityType.USER_UPDATED,
                description = "${user.firstName} ${user.lastName} profile updated",
                userId = user.id,
                userName = "${user.firstName} ${user.lastName}",
                timestamp = user.updatedAt
            )
        }
    }

    fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        val random = SecureRandom()
        return (1..12)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
}