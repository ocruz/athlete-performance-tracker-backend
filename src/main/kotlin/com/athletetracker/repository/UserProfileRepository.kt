package com.athletetracker.repository

import com.athletetracker.entity.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserProfileRepository : JpaRepository<UserProfile, Long> {
    
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId")
    fun findByUserId(@Param("userId") userId: Long): Optional<UserProfile>
    
    @Query("SELECT up FROM UserProfile up WHERE up.user.email = :email")
    fun findByUserEmail(@Param("email") email: String): Optional<UserProfile>
    
    fun existsByUserId(userId: Long): Boolean
}