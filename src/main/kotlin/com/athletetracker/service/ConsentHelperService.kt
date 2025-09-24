package com.athletetracker.service

import com.athletetracker.dto.UserConsentDto
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ConsentHelperService(
    private val authorizationConsentService: OAuth2AuthorizationConsentService,
    private val oauth2ScopeService: OAuth2ScopeService,
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private const val FIND_USER_CONSENTS_SQL = """
            SELECT oac.registered_client_id, oac.principal_name, oac.authorities, orc.client_name
            FROM oauth2_authorization_consent oac
            JOIN oauth2_registered_client orc ON oac.registered_client_id = orc.id
            WHERE oac.principal_name = ?
            ORDER BY orc.client_name
        """
    }

    /**
     * Save user consent for specific scopes
     * This is a convenience method that creates the proper OAuth2AuthorizationConsent object
     */
    fun saveConsent(clientId: String, principalName: String, approvedScopes: List<String>) {
        val authorities = approvedScopes.map { "SCOPE_$it" }.toSet()
        
        val authorizationConsent = OAuth2AuthorizationConsent.withId(clientId, principalName)
            .authorities { it.addAll(authorities.map { authority -> 
                org.springframework.security.core.authority.SimpleGrantedAuthority(authority) 
            }) }
            .build()

        authorizationConsentService.save(authorizationConsent)
        
        // Log consent for audit purposes
        logConsentActivity(clientId, principalName, "GRANTED", approvedScopes)
    }

    /**
     * Revoke user consent for a specific client
     */
    fun revokeConsent(clientId: String, principalName: String) {
        val existingConsent = authorizationConsentService.findById(clientId, principalName)
        if (existingConsent != null) {
            val revokedScopes = existingConsent.authorities
                .map { it.authority.removePrefix("SCOPE_") }
            
            authorizationConsentService.remove(existingConsent)
            
            // Log revocation for audit purposes
            logConsentActivity(clientId, principalName, "REVOKED", revokedScopes)
        }
    }

    /**
     * Get all consents for a user
     */
    @Transactional(readOnly = true)
    fun getUserConsents(principalName: String): List<UserConsentDto> {
        return jdbcTemplate.query(
            FIND_USER_CONSENTS_SQL,
            { rs, _ ->
                val authorities = rs.getString("authorities")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .map { it.removePrefix("SCOPE_") }

                val scopeDescriptions = oauth2ScopeService.getScopeDescriptions(authorities)

                UserConsentDto(
                    clientId = rs.getString("registered_client_id"),
                    clientName = rs.getString("client_name"),
                    principalName = rs.getString("principal_name"),
                    grantedScopes = authorities,
                    scopeDescriptions = scopeDescriptions,
                    grantedAt = LocalDateTime.now().toString(), // This should come from an actual timestamp column
                    canRevoke = true
                )
            },
            principalName
        )
    }

    /**
     * Check if user has previously consented to specific scopes for a client
     */
    @Transactional(readOnly = true)
    fun hasConsentedToScopes(clientId: String, principalName: String, requestedScopes: List<String>): Boolean {
        val existingConsent = authorizationConsentService.findById(clientId, principalName) ?: return false
        
        val consentedScopes = existingConsent.authorities
            .map { it.authority.removePrefix("SCOPE_") }
            .toSet()
        
        return requestedScopes.all { it in consentedScopes }
    }

    /**
     * Get scopes that haven't been consented to yet
     */
    @Transactional(readOnly = true)
    fun getUnconsentedScopes(clientId: String, principalName: String, requestedScopes: List<String>): List<String> {
        val existingConsent = authorizationConsentService.findById(clientId, principalName)
        if (existingConsent == null) {
            return requestedScopes
        }
        
        val consentedScopes = existingConsent.authorities
            .map { it.authority.removePrefix("SCOPE_") }
            .toSet()
        
        return requestedScopes.filter { it !in consentedScopes }
    }

    /**
     * Update existing consent with additional scopes
     */
    fun updateConsent(clientId: String, principalName: String, additionalScopes: List<String>) {
        val existingConsent = authorizationConsentService.findById(clientId, principalName)
        val currentScopes = existingConsent?.authorities
            ?.map { it.authority.removePrefix("SCOPE_") }
            ?.toMutableSet() ?: mutableSetOf()
        
        currentScopes.addAll(additionalScopes)
        saveConsent(clientId, principalName, currentScopes.toList())
    }

    /**
     * Log consent activity for audit purposes
     */
    private fun logConsentActivity(
        clientId: String, 
        principalName: String, 
        action: String, 
        scopes: List<String>
    ) {
        // This could be enhanced to use the OAuth2AccessLog entity
        println("CONSENT_AUDIT: $action - Client: $clientId, User: $principalName, Scopes: ${scopes.joinToString(",")}")
    }
}