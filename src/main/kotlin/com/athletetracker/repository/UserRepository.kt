package com.athletetracker.repository

import com.athletetracker.entity.User
import com.athletetracker.entity.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByRole(role: UserRole): List<User>
    fun findByIsActiveTrue(): List<User>
    fun existsByEmail(email: String): Boolean
}