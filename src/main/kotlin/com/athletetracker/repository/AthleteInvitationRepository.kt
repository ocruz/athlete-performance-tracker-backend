package com.athletetracker.repository

import com.athletetracker.entity.AthleteInvitation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AthleteInvitationRepository : JpaRepository<AthleteInvitation, Long> {
    
    /**
     * Find a valid, unused invitation by token
     */
    fun findByInvitationTokenAndIsUsedFalse(token: String): AthleteInvitation?
    
    /**
     * Find the most recent invitation for an athlete
     */
    fun findTopByAthleteIdOrderByCreatedAtDesc(athleteId: Long): AthleteInvitation?
    
    /**
     * Find all invitations for an athlete
     */
    fun findByAthleteIdOrderByCreatedAtDesc(athleteId: Long): List<AthleteInvitation>
    
    /**
     * Find invitation by athlete and email
     */
    fun findByAthleteIdAndEmail(athleteId: Long, email: String): List<AthleteInvitation>
    
    /**
     * Check if there's an active (unused and not expired) invitation for an athlete
     */
    @Query("""
        SELECT i FROM AthleteInvitation i 
        WHERE i.athleteId = :athleteId 
        AND i.isUsed = false 
        AND i.expiresAt > :now
        ORDER BY i.createdAt DESC
    """)
    fun findActiveInvitationForAthlete(
        @Param("athleteId") athleteId: Long, 
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): AthleteInvitation?
    
    /**
     * Find all expired, unused invitations for cleanup
     */
    @Query("""
        SELECT i FROM AthleteInvitation i 
        WHERE i.isUsed = false 
        AND i.expiresAt < :now
    """)
    fun findExpiredInvitations(@Param("now") now: LocalDateTime = LocalDateTime.now()): List<AthleteInvitation>
    
    /**
     * Count active invitations by creator
     */
    @Query("""
        SELECT COUNT(i) FROM AthleteInvitation i 
        WHERE i.createdBy = :createdBy 
        AND i.isUsed = false 
        AND i.expiresAt > :now
    """)
    fun countActiveInvitationsByCreator(
        @Param("createdBy") createdBy: Long,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Long
}