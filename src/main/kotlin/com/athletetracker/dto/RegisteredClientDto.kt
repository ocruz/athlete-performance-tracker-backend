package com.athletetracker.dto

import jakarta.validation.constraints.*
import java.time.Instant

/**
 * Simplified DTOs for managing OAuth2 registered clients using Spring's built-in RegisteredClient
 */

// Request DTOs for creating/updating registered clients
data class CreateRegisteredClientRequest(
    @field:NotBlank(message = "Client name is required")
    @field:Size(max = 200, message = "Client name must not exceed 200 characters")
    val clientName: String,

    @field:NotEmpty(message = "At least one redirect URI is required")
    val redirectUris: Set<String>,

    val postLogoutRedirectUris: Set<String> = emptySet(),

    @field:NotEmpty(message = "At least one scope is required")
    val scopes: Set<String>,

    @field:NotNull(message = "Client authentication method is required")
    val clientAuthenticationMethod: String = "client_secret_basic", // client_secret_basic, client_secret_post, none

    val authorizationGrantTypes: Set<String> = setOf("authorization_code", "refresh_token"),

    val requireAuthorizationConsent: Boolean = true,
    val requireProofKey: Boolean = false, // PKCE - set to true for public clients
    
    // Token settings
    val accessTokenTimeToLive: Long = 900, // 15 minutes in seconds
    val refreshTokenTimeToLive: Long = 3600, // 1 hour in seconds
    val reuseRefreshTokens: Boolean = false
)

data class UpdateRegisteredClientRequest(
    val clientName: String? = null,
    val redirectUris: Set<String>? = null,
    val postLogoutRedirectUris: Set<String>? = null,
    val scopes: Set<String>? = null,
    val requireAuthorizationConsent: Boolean? = null,
    val requireProofKey: Boolean? = null,
    val accessTokenTimeToLive: Long? = null,
    val refreshTokenTimeToLive: Long? = null,
    val reuseRefreshTokens: Boolean? = null
)

// Response DTOs
data class RegisteredClientDto(
    val id: String,
    val clientId: String,
    val clientIdIssuedAt: Instant?,
    val clientName: String,
    val clientAuthenticationMethods: Set<String>,
    val authorizationGrantTypes: Set<String>,
    val redirectUris: Set<String>,
    val postLogoutRedirectUris: Set<String>,
    val scopes: Set<String>,
    
    // Client Settings
    val requireAuthorizationConsent: Boolean,
    val requireProofKey: Boolean,
    
    // Token Settings  
    val accessTokenTimeToLive: Long, // seconds
    val refreshTokenTimeToLive: Long, // seconds
    val reuseRefreshTokens: Boolean,
    
    // Only include client secret in creation response
    val clientSecret: String? = null
)

data class ClientCredentialsDto(
    val clientId: String,
    val clientSecret: String,
    val message: String = "Client created successfully. Store the client secret securely - it won't be shown again."
)

data class RegisteredClientListResponse(
    val clients: List<RegisteredClientDto>,
    val total: Int
)

// Search and filter
data class ClientSearchRequest(
    val clientName: String? = null,
    val scope: String? = null,
    val page: Int = 0,
    val size: Int = 20
)

// OAuth2 scope DTOs (moved from deleted ClientApplicationDto)
data class OAuth2ScopeDto(
    val id: Long,
    val scopeName: String,
    val displayName: String,
    val description: String,
    val category: String,
    val isSensitive: Boolean,
    val isDefault: Boolean
)

data class AvailableScopesResponse(
    val scopes: List<OAuth2ScopeDto>,
    val categories: Map<String, List<OAuth2ScopeDto>>
)

// User consent DTOs (moved from deleted ClientApplicationDto)
data class UserConsentDto(
    val clientId: String,
    val clientName: String,
    val principalName: String,
    val grantedScopes: List<String>,
    val scopeDescriptions: Map<String, com.athletetracker.service.OAuth2ScopeService.ScopeDescription>,
    val grantedAt: String,
    val canRevoke: Boolean
)