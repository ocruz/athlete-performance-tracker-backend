package com.athletetracker.repository

import com.athletetracker.entity.User
import com.athletetracker.entity.UserRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByRole(role: UserRole): List<User>
    fun findByIsActiveTrue(): List<User>
    fun existsByEmail(email: String): Boolean
    
    // Count methods for dashboard statistics
    fun countByRole(role: UserRole): Long
    fun countByIsActive(isActive: Boolean): Long
    fun countByCreatedAtAfter(date: LocalDateTime): Long
    
    // Pagination and filtering methods
    fun findByRoleAndIsActiveOrderByCreatedAtDesc(role: UserRole, isActive: Boolean, pageable: Pageable): Page<User>
    fun findByIsActiveOrderByCreatedAtDesc(isActive: Boolean, pageable: Pageable): Page<User>
    
    // Recent activity method
    fun findTop10ByOrderByUpdatedAtDesc(): List<User>
    
    // Search method
    @Query("SELECT u FROM User u WHERE " +
           "(:searchQuery IS NULL OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive)")
    fun findBySearchCriteria(
        @Param("searchQuery") searchQuery: String?,
        @Param("role") role: UserRole?,
        @Param("isActive") isActive: Boolean?,
        pageable: Pageable
    ): Page<User>
}