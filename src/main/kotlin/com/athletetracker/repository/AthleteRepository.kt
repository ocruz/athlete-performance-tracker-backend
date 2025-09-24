package com.athletetracker.repository

import com.athletetracker.entity.Athlete
import com.athletetracker.entity.Sport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AthleteRepository : JpaRepository<Athlete, Long> {
    fun findBySportAndIsActiveTrue(sport: Sport): List<Athlete>
    fun findByIsActiveTrue(): List<Athlete>
    fun findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        firstName: String, 
        lastName: String
    ): List<Athlete>
    
    @Query("SELECT a FROM Athlete a WHERE a.isActive = true AND " +
           "(LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')))")
    fun searchByName(@Param("query") query: String): List<Athlete>
    
    fun countBySportAndIsActiveTrue(sport: Sport): Long
    fun countByIsActiveTrue(): Long
    fun countBySport(sport: Sport): Long
    fun findByEmail(email: String): Athlete?
    fun findByUserId(userId: Long): Athlete?
}