package com.athletetracker.service

import com.athletetracker.dto.OAuth2ScopeDto
import com.athletetracker.entity.OAuth2Scope
import com.athletetracker.entity.ScopeCategory
import com.athletetracker.repository.OAuth2ScopeRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OAuth2ScopeService(
    private val oauth2ScopeRepository: OAuth2ScopeRepository
) {

    /**
     * Get all available OAuth2 scopes
     */
    fun getAllScopes(): List<OAuth2ScopeDto> {
        return oauth2ScopeRepository.findAll().map { mapToDto(it) }
    }

    /**
     * Get scopes by category
     */
    fun getScopesByCategory(): Map<String, List<OAuth2ScopeDto>> {
        val allScopes = oauth2ScopeRepository.findAll()
        return allScopes.groupBy { it.category.name }
            .mapValues { (_, scopes) -> scopes.map { mapToDto(it) } }
    }

    /**
     * Get default scopes that should be included in all client registrations
     */
    fun getDefaultScopes(): List<OAuth2ScopeDto> {
        return oauth2ScopeRepository.findByIsDefaultTrue().map { mapToDto(it) }
    }

    /**
     * Get sensitive scopes that require additional consent
     */
    fun getSensitiveScopes(): List<OAuth2ScopeDto> {
        return oauth2ScopeRepository.findByIsSensitiveTrue().map { mapToDto(it) }
    }

    /**
     * Get scopes by category
     */
    fun getScopesByCategory(category: ScopeCategory): List<OAuth2ScopeDto> {
        return oauth2ScopeRepository.findByCategory(category).map { mapToDto(it) }
    }

    /**
     * Get scopes by names
     */
    fun getScopesByNames(scopeNames: List<String>): List<OAuth2ScopeDto> {
        return oauth2ScopeRepository.findByScopeNamesOrdered(scopeNames).map { mapToDto(it) }
    }

    /**
     * Validate that all requested scopes exist and are valid
     */
    fun validateScopes(requestedScopes: List<String>): ScopeValidationResult {
        val validScopes = oauth2ScopeRepository.findByScopeNameIn(requestedScopes)
        val validScopeNames = validScopes.map { it.scopeName }.toSet()
        val invalidScopes = requestedScopes.filter { it !in validScopeNames }
        
        val sensitiveScopes = validScopes.filter { it.isSensitive }
        
        return ScopeValidationResult(
            isValid = invalidScopes.isEmpty(),
            validScopes = validScopes.map { mapToDto(it) },
            invalidScopes = invalidScopes,
            sensitiveScopes = sensitiveScopes.map { mapToDto(it) },
            requiresAdditionalConsent = sensitiveScopes.isNotEmpty()
        )
    }

    /**
     * Check if a user should be prompted for consent for the requested scopes
     * This considers sensitive scopes and user's previous consent decisions
     */
    fun requiresConsent(
        requestedScopes: List<String>,
        clientId: String,
        userId: Long,
        isClientTrusted: Boolean = false
    ): ConsentRequirement {
        if (isClientTrusted) {
            return ConsentRequirement(
                requiresConsent = false,
                reason = "Client is trusted"
            )
        }

        val scopeValidation = validateScopes(requestedScopes)
        if (!scopeValidation.isValid) {
            return ConsentRequirement(
                requiresConsent = true,
                reason = "Invalid scopes requested",
                invalidScopes = scopeValidation.invalidScopes
            )
        }

        if (scopeValidation.requiresAdditionalConsent) {
            return ConsentRequirement(
                requiresConsent = true,
                reason = "Sensitive scopes require consent",
                sensitiveScopes = scopeValidation.sensitiveScopes
            )
        }

        // For now, always require consent for non-trusted clients
        // In the future, this could check previous consent decisions
        return ConsentRequirement(
            requiresConsent = true,
            reason = "First-time authorization",
            requestedScopes = scopeValidation.validScopes
        )
    }

    /**
     * Get user-friendly descriptions for scopes to display in consent screen
     */
    fun getScopeDescriptions(scopeNames: List<String>): Map<String, ScopeDescription> {
        val scopes = oauth2ScopeRepository.findByScopeNameIn(scopeNames)
        return scopes.associate { scope ->
            scope.scopeName to ScopeDescription(
                displayName = scope.displayName,
                description = scope.description,
                category = scope.category.name,
                isSensitive = scope.isSensitive
            )
        }
    }

    private fun mapToDto(scope: OAuth2Scope): OAuth2ScopeDto {
        return OAuth2ScopeDto(
            id = scope.id,
            scopeName = scope.scopeName,
            displayName = scope.displayName,
            description = scope.description,
            category = scope.category.name,
            isSensitive = scope.isSensitive,
            isDefault = scope.isDefault
        )
    }

    data class ScopeValidationResult(
        val isValid: Boolean,
        val validScopes: List<OAuth2ScopeDto>,
        val invalidScopes: List<String>,
        val sensitiveScopes: List<OAuth2ScopeDto>,
        val requiresAdditionalConsent: Boolean
    )

    data class ConsentRequirement(
        val requiresConsent: Boolean,
        val reason: String,
        val requestedScopes: List<OAuth2ScopeDto> = emptyList(),
        val sensitiveScopes: List<OAuth2ScopeDto> = emptyList(),
        val invalidScopes: List<String> = emptyList()
    )

    data class ScopeDescription(
        val displayName: String,
        val description: String,
        val category: String,
        val isSensitive: Boolean
    )
}