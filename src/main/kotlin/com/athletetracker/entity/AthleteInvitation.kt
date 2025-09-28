package com.athletetracker.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "athlete_invitations")
data class AthleteInvitation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val athleteId: Long,
    
    @Column(nullable = false, unique = true)
    val invitationToken: String,
    
    @Column(nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val expiresAt: LocalDateTime,
    
    @Column(nullable = false)
    val isUsed: Boolean = false,
    
    @Column
    val usedAt: LocalDateTime? = null,
    
    @Column
    val createdBy: Long? = null // User ID of the coach who created this invitation
) {
    companion object {
        fun create(athleteId: Long, email: String, createdBy: Long? = null): AthleteInvitation {
            return AthleteInvitation(
                athleteId = athleteId,
                invitationToken = UUID.randomUUID().toString(),
                email = email,
                expiresAt = LocalDateTime.now().plusDays(7), // 7 day expiration
                createdBy = createdBy
            )
        }
    }
    
    val isExpired: Boolean
        get() = LocalDateTime.now().isAfter(expiresAt)
    
    val isValid: Boolean
        get() = !isUsed && !isExpired
}