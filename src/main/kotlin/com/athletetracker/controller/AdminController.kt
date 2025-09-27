package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.service.AdminService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val adminService: AdminService
) {

    // Dashboard Statistics
    @GetMapping("/dashboard/stats")
    fun getDashboardStats(): ResponseEntity<AdminDashboardStatsResponse> {
        val stats = adminService.getDashboardStats()
        return ResponseEntity.ok(stats)
    }

    // User Management Endpoints

    @GetMapping("/users")
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) isActive: Boolean?,
        @RequestParam(required = false) sport: String?,
        @RequestParam(required = false) searchQuery: String?
    ): ResponseEntity<UserListResponse> {
        val roleEnum = role?.let { 
            try { 
                com.athletetracker.entity.UserRole.valueOf(it.uppercase()) 
            } catch (e: IllegalArgumentException) { 
                null 
            } 
        }
        val sportEnum = sport?.let { 
            try { 
                com.athletetracker.entity.Sport.valueOf(it.uppercase()) 
            } catch (e: IllegalArgumentException) { 
                null 
            } 
        }

        val request = UserListRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection,
            role = roleEnum,
            isActive = isActive,
            sport = sportEnum,
            searchQuery = searchQuery
        )

        val response = adminService.getAllUsers(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<AdminUserResponse> {
        val user = adminService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @PostMapping("/users/coach")
    fun createCoach(@Valid @RequestBody request: CreateCoachRequest): ResponseEntity<UserCreationResponse> {
        val response = adminService.createCoach(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/users/athlete")
    fun createAthlete(@Valid @RequestBody request: CreateAthleteByAdminRequest): ResponseEntity<UserCreationResponse> {
        val response = adminService.createAthlete(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/users/{id}")
    fun updateUserProfile(
        @PathVariable id: Long,
        @Valid @RequestBody request: AdminUpdateUserProfileRequest
    ): ResponseEntity<AdminUserResponse> {
        val user = adminService.updateUserProfile(id, request)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/users/{id}/password")
    fun changeUserPassword(
        @PathVariable id: Long,
        @Valid @RequestBody request: AdminChangePasswordRequest
    ): ResponseEntity<Map<String, String>> {
        val message = adminService.changeUserPassword(id, request)
        return ResponseEntity.ok(mapOf("message" to message))
    }

    @PutMapping("/users/{id}/status")
    fun toggleUserStatus(@PathVariable id: Long): ResponseEntity<AdminUserResponse> {
        val user = adminService.toggleUserStatus(id)
        return ResponseEntity.ok(user)
    }

    @PostMapping("/users/bulk-action")
    fun performBulkAction(@Valid @RequestBody request: BulkUserActionRequest): ResponseEntity<Map<String, String>> {
        val message = adminService.performBulkAction(request)
        return ResponseEntity.ok(mapOf("message" to message))
    }

    // Utility Endpoints

    @PostMapping("/users/generate-password")
    fun generateRandomPassword(): ResponseEntity<Map<String, String>> {
        val password = adminService.generateRandomPassword()
        return ResponseEntity.ok(mapOf("password" to password))
    }

    @GetMapping("/users/roles")
    fun getUserRoles(): ResponseEntity<List<String>> {
        val roles = com.athletetracker.entity.UserRole.values().map { it.name }
        return ResponseEntity.ok(roles)
    }

    @GetMapping("/users/sports")
    fun getSports(): ResponseEntity<List<String>> {
        val sports = com.athletetracker.entity.Sport.values().map { it.name }
        return ResponseEntity.ok(sports)
    }

    // Exception Handlers
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to "An error occurred while processing your request"))
    }
}