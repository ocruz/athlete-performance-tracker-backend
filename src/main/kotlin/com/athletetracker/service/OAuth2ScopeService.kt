package com.athletetracker.service

import com.athletetracker.entity.OAuth2Scope
import com.athletetracker.entity.ScopeCategory
import com.athletetracker.repository.OAuth2ScopeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OAuth2ScopeService(
    private val scopeRepository: OAuth2ScopeRepository
) {
    
    @Transactional(readOnly = true)
    fun getAllScopes(): List<OAuth2Scope> {
        return scopeRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getScopesByCategory(category: ScopeCategory): List<OAuth2Scope> {
        return scopeRepository.findByCategory(category)
    }
    
    @Transactional(readOnly = true)
    fun getDefaultScopes(): List<OAuth2Scope> {
        return scopeRepository.findByIsDefaultTrue()
    }
    
    @Transactional(readOnly = true)
    fun getSensitiveScopes(): List<OAuth2Scope> {
        return scopeRepository.findByIsSensitiveTrue()
    }
    
    @Transactional(readOnly = true)
    fun validateScopes(requestedScopes: List<String>): List<OAuth2Scope> {
        return scopeRepository.findByScopeNameIn(requestedScopes)
    }
    
    @Transactional(readOnly = true)
    fun getScopeDescription(scopeName: String): String? {
        return scopeRepository.findByScopeName(scopeName)?.description
    }
    
    /**
     * Validates that all requested scopes exist and returns validation results
     */
    @Transactional(readOnly = true)
    fun validateScopeRequest(requestedScopes: List<String>): ScopeValidationResult {
        val existingScopes = validateScopes(requestedScopes)
        val existingScopeNames = existingScopes.map { it.scopeName }
        val invalidScopes = requestedScopes - existingScopeNames.toSet()
        val sensitiveScopes = existingScopes.filter { it.isSensitive }
        
        return ScopeValidationResult(
            validScopes = existingScopes,
            invalidScopes = invalidScopes,
            sensitiveScopes = sensitiveScopes,
            isValid = invalidScopes.isEmpty()
        )
    }
    
    /**
     * Gets the recommended scopes for a new client application
     */
    @Transactional(readOnly = true)
    fun getRecommendedScopesForClient(): List<OAuth2Scope> {
        return getDefaultScopes()
    }
    
    /**
     * Groups scopes by category for display purposes
     */
    @Transactional(readOnly = true)
    fun getScopesGroupedByCategory(): Map<ScopeCategory, List<OAuth2Scope>> {
        return getAllScopes().groupBy { it.category }
    }
}

data class ScopeValidationResult(
    val validScopes: List<OAuth2Scope>,
    val invalidScopes: List<String>,
    val sensitiveScopes: List<OAuth2Scope>,
    val isValid: Boolean
)