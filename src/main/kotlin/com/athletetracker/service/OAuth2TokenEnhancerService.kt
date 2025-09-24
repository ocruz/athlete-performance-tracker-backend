package com.athletetracker.service

import com.athletetracker.entity.User
import com.athletetracker.entity.UserRole
import com.athletetracker.repository.AthleteRepository
import com.athletetracker.repository.CoachRepository
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.stereotype.Component

@Component
class OAuth2TokenEnhancerService(
    private val athleteRepository: AthleteRepository,
    private val coachRepository: CoachRepository
) : OAuth2TokenCustomizer<JwtEncodingContext> {

    override fun customize(context: JwtEncodingContext) {
        val authentication = context.getPrincipal<Authentication>()
        
        if (authentication?.principal is User) {
            val user = authentication.principal as User
            
            // Add basic user information
            context.claims.claim("userId", user.id)
            context.claims.claim("username", user.email)
            context.claims.claim("fullName", user.fullName)
            context.claims.claim("role", user.role.name)
            context.claims.claim("authorities", authentication.authorities.map { it.authority })

            // Add profile-specific IDs based on user role
            when (user.role) {
                UserRole.ATHLETE -> {
                    val athlete = athleteRepository.findByUserId(user.id)
                    if (athlete != null) {
                        context.claims.claim("athleteId", athlete.id)
                        context.claims.claim("sport", athlete.sport.name)
                        context.claims.claim("position", athlete.position)
                    }
                }
                UserRole.COACH -> {
                    val coach = coachRepository.findByUserId(user.id)
                    if (coach.isPresent) {
                        context.claims.claim("coachId", coach.get().id)
                        context.claims.claim("specializations", coach.get().specializations)
                        context.claims.claim("preferredSports", coach.get().preferredSports)
                    }
                }
                UserRole.ADMIN -> {
                    // Admins might need both athlete and coach access for management
                    context.claims.claim("isAdmin", true)
                }
            }

            // Add standard OAuth2 claims
            context.claims.claim("iss", context.authorizationGrantType)
            context.claims.claim("aud", context.registeredClient.clientId)
            
            // Add custom claims for mobile app integration
            if (context.registeredClient.clientId.contains("mobile")) {
                context.claims.claim("deviceType", "mobile")
                context.claims.claim("apiVersion", "v1")
            }
        }
    }

    /**
     * Extract athlete ID from user safely
     */
    fun getAthleteIdForUser(userId: Long): Long? {
        return athleteRepository.findByUserId(userId)?.id
    }

    /**
     * Extract coach ID from user safely
     */
    fun getCoachIdForUser(userId: Long): Long? {
        return coachRepository.findByUserId(userId).orElse(null)?.id
    }
}