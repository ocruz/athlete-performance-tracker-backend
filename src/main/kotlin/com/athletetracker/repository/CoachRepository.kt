package com.athletetracker.repository

import com.athletetracker.entity.Coach
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CoachRepository : JpaRepository<Coach, Long> {
    
    fun findByUserId(userId: Long): Optional<Coach>
    
    fun findByUserIdAndIsActive(userId: Long, isActive: Boolean): Optional<Coach>
    
    fun existsByUserId(userId: Long): Boolean
    
    fun findAllByIsActive(isActive: Boolean): List<Coach>
    
    @Query("SELECT c FROM Coach c WHERE c.email = :email")
    fun findByEmail(@Param("email") email: String): Optional<Coach>
    
    @Query("SELECT c FROM Coach c WHERE c.specializations ILIKE %:specialization% AND c.isActive = true")
    fun findBySpecialization(@Param("specialization") specialization: String): List<Coach>
    
    @Query("SELECT c FROM Coach c WHERE c.preferredSports ILIKE %:sport% AND c.isActive = true")
    fun findByPreferredSport(@Param("sport") sport: String): List<Coach>
    
    @Query("SELECT c FROM Coach c WHERE c.yearsExperience >= :minYears AND c.isActive = true")
    fun findByMinYearsExperience(@Param("minYears") minYears: Int): List<Coach>
    
    @Query("""
        SELECT c FROM Coach c 
        WHERE (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) 
               OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.specializations) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.preferredSports) LIKE LOWER(CONCAT('%', :query, '%')))
        AND c.isActive = true
    """)
    fun searchCoaches(@Param("query") query: String): List<Coach>
}