package com.athletetracker.repository

import com.athletetracker.entity.Assessment
import com.athletetracker.entity.AssessmentCategory
import com.athletetracker.entity.Sport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AssessmentRepository : JpaRepository<Assessment, Long> {
    
    // Find active assessments
    fun findByIsActiveTrue(): List<Assessment>
    
    // Find assessments by category
    fun findByCategoryAndIsActiveTrue(category: AssessmentCategory): List<Assessment>
    
    // Find assessments by sport
    fun findBySportAndIsActiveTrue(sport: Sport): List<Assessment>
    
    // Find template assessments
    fun findByIsTemplateAndIsActiveTrue(isTemplate: Boolean): List<Assessment>
    
    // Find assessments by sport and category
    fun findBySportAndCategoryAndIsActiveTrue(sport: Sport, category: AssessmentCategory): List<Assessment>
    
    // Search assessments by name or description
    @Query("SELECT a FROM Assessment a WHERE a.isActive = true AND " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    fun searchByNameOrDescription(@Param("query") query: String): List<Assessment>
    
    // Find assessments created by specific user
    @Query("SELECT a FROM Assessment a WHERE a.createdBy.id = :userId AND a.isActive = true")
    fun findByCreatedByAndIsActiveTrue(@Param("userId") userId: Long): List<Assessment>
    
    // Find popular assessments (most used)
    @Query("SELECT a FROM Assessment a LEFT JOIN a.assessmentResults ar " +
           "WHERE a.isActive = true " +
           "GROUP BY a.id " +
           "ORDER BY COUNT(ar.id) DESC")
    fun findMostUsedAssessments(): List<Assessment>
    
    // Find assessments by equipment required
    @Query("SELECT a FROM Assessment a WHERE a.isActive = true AND " +
           "(:equipment IS NULL OR LOWER(a.equipmentRequired) LIKE LOWER(CONCAT('%', :equipment, '%')))")
    fun findByEquipmentRequired(@Param("equipment") equipment: String?): List<Assessment>
    
    // Find assessments by estimated duration range
    @Query("SELECT a FROM Assessment a WHERE a.isActive = true AND " +
           "(:minDuration IS NULL OR a.estimatedDuration >= :minDuration) AND " +
           "(:maxDuration IS NULL OR a.estimatedDuration <= :maxDuration)")
    fun findByDurationRange(@Param("minDuration") minDuration: Int?, 
                           @Param("maxDuration") maxDuration: Int?): List<Assessment>
}