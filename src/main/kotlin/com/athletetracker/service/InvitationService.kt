package com.athletetracker.service

import com.athletetracker.entity.AthleteInvitation
import com.athletetracker.repository.AthleteInvitationRepository
import com.athletetracker.repository.AthleteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class InvitationService(
    private val invitationRepository: AthleteInvitationRepository,
    private val athleteRepository: AthleteRepository,
    private val emailService: EmailService
) {
    
    /**
     * Create and send an invitation for an athlete
     */
    fun createInvitation(athleteId: Long, email: String, createdBy: Long? = null): AthleteInvitation {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        
        // Check if athlete already has a user account
        if (athlete.userId != null) {
            throw IllegalArgumentException("Athlete already has a user account")
        }
        
        // Check if there's already an active invitation
        val activeInvitation = invitationRepository.findActiveInvitationForAthlete(athleteId)
        if (activeInvitation != null) {
            throw IllegalArgumentException("Athlete already has an active invitation. Use resend instead.")
        }
        
        // Create new invitation
        val invitation = AthleteInvitation.create(
            athleteId = athleteId,
            email = email,
            createdBy = createdBy
        )
        
        val savedInvitation = invitationRepository.save(invitation)
        
        // Send invitation email
        sendInvitationEmail(savedInvitation, athlete.firstName + " " + athlete.lastName)
        
        return savedInvitation
    }
    
    /**
     * Resend an existing invitation or create a new one if expired
     */
    fun resendInvitation(athleteId: Long, coachUserId: Long? = null): AthleteInvitation {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        
        if (athlete.userId != null) {
            throw IllegalArgumentException("Athlete already has a user account")
        }
        
        if (athlete.email.isNullOrBlank()) {
            throw IllegalArgumentException("Athlete must have an email address to receive invitations")
        }
        
        // Check for existing active invitation
        val activeInvitation = invitationRepository.findActiveInvitationForAthlete(athleteId)
        
        return if (activeInvitation != null) {
            // Resend existing invitation
            sendInvitationEmail(activeInvitation, athlete.fullName, isReminder = true)
            activeInvitation
        } else {
            // Create new invitation (previous one expired or was used)
            createInvitation(athleteId, athlete.email, coachUserId)
        }
    }
    
    /**
     * Validate an invitation token
     */
    @Transactional(readOnly = true)
    fun validateInvitation(token: String): AthleteInvitation? {
        val invitation = invitationRepository.findByInvitationTokenAndIsUsedFalse(token)
        return invitation?.takeIf { it.isValid }
    }
    
    /**
     * Mark an invitation as used
     */
    fun markInvitationAsUsed(invitationId: Long) {
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { IllegalArgumentException("Invitation not found with id: $invitationId") }
        
        val updatedInvitation = invitation.copy(
            isUsed = true,
            usedAt = LocalDateTime.now()
        )
        
        invitationRepository.save(updatedInvitation)
    }
    
    /**
     * Get invitation status for an athlete
     */
    @Transactional(readOnly = true)
    fun getInvitationStatus(athleteId: Long): InvitationStatus {
        val athlete = athleteRepository.findById(athleteId)
            .orElseThrow { IllegalArgumentException("Athlete not found with id: $athleteId") }
        
        return when {
            athlete.userId != null -> InvitationStatus.ACCOUNT_CREATED
            athlete.email.isNullOrBlank() -> InvitationStatus.NO_EMAIL
            else -> {
                val activeInvitation = invitationRepository.findActiveInvitationForAthlete(athleteId)
                if (activeInvitation != null) {
                    InvitationStatus.INVITATION_SENT
                } else {
                    val anyInvitation = invitationRepository.findTopByAthleteIdOrderByCreatedAtDesc(athleteId)
                    if (anyInvitation != null) {
                        if (anyInvitation.isUsed) {
                            InvitationStatus.INVITATION_USED
                        } else {
                            InvitationStatus.INVITATION_EXPIRED
                        }
                    } else {
                        InvitationStatus.NO_INVITATION
                    }
                }
            }
        }
    }
    
    /**
     * Get all invitations for an athlete
     */
    @Transactional(readOnly = true)
    fun getInvitationsForAthlete(athleteId: Long): List<AthleteInvitation> {
        return invitationRepository.findByAthleteIdOrderByCreatedAtDesc(athleteId)
    }
    
    /**
     * Clean up expired invitations (can be run as a scheduled job)
     */
    fun cleanupExpiredInvitations(): Int {
        val expiredInvitations = invitationRepository.findExpiredInvitations()
        // For now, we just return the count. In the future, we might actually delete them
        // or mark them as expired for audit purposes
        return expiredInvitations.size
    }
    
    private fun sendInvitationEmail(
        invitation: AthleteInvitation, 
        athleteName: String, 
        coachName: String? = null,
        isReminder: Boolean = false
    ) {
        if (isReminder) {
            emailService.sendInvitationReminder(
                email = invitation.email,
                token = invitation.invitationToken,
                athleteName = athleteName,
                coachName = coachName,
                expiresAt = invitation.expiresAt
            )
        } else {
            emailService.sendAthleteInvitation(
                email = invitation.email,
                token = invitation.invitationToken,
                athleteName = athleteName,
                coachName = coachName,
                expiresAt = invitation.expiresAt
            )
        }
    }
}

enum class InvitationStatus {
    NO_EMAIL,           // Athlete has no email address
    NO_INVITATION,      // Athlete has email but no invitation sent
    INVITATION_SENT,    // Active invitation exists
    INVITATION_EXPIRED, // Last invitation expired
    INVITATION_USED,    // Invitation was used but account creation failed
    ACCOUNT_CREATED     // Athlete successfully created account
}