package com.athletetracker.repository

import com.athletetracker.entity.Program
import com.athletetracker.entity.Sport
import com.athletetracker.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProgramRepository : JpaRepository<Program, Long> {
    fun findByCreatedByOrderByCreatedAtDesc(createdBy: User): List<Program>
    fun findByCreatedById(createdById: Long): List<Program>
    fun findByIsActiveTrue(): List<Program>
    fun findByIsActiveTrueAndIsTemplateTrue(): List<Program>
    fun findByIsActiveTrueAndSport(sport: Sport): List<Program>
    fun findByIsActiveTrueAndSportAndIsTemplateTrue(sport: Sport): List<Program>
    
    @Query("SELECT p FROM Program p WHERE p.isActive = true AND " +
           "(p.name LIKE %:search% OR p.description LIKE %:search%)")
    fun searchActivePrograms(@Param("search") search: String): List<Program>
    
    fun findByIsActiveTrueAndDifficultyLevel(difficultyLevel: String): List<Program>
}