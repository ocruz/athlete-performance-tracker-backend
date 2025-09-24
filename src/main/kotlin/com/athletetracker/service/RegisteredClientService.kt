package com.athletetracker.service

import com.athletetracker.dto.*
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
@Transactional
class RegisteredClientService(
    private val registeredClientRepository: RegisteredClientRepository,
    private val clientSecretService: ClientSecretService,
    private val oauth2ScopeService: OAuth2ScopeService
) {

    /**
     * Create a new registered client
     */
    fun createRegisteredClient(request: CreateRegisteredClientRequest): ClientCredentialsDto {
        // Validate scopes
        val scopeValidation = oauth2ScopeService.validateScopes(request.scopes.toList())
        if (!scopeValidation.isValid) {
            throw IllegalArgumentException("Invalid scopes: ${scopeValidation.invalidScopes.joinToString(", ")}")
        }

        // Generate client credentials
        val credentials = clientSecretService.generateClientCredentials("client")

        // Map authentication methods
        val clientAuthMethod = when (request.clientAuthenticationMethod.lowercase()) {
            "client_secret_basic" -> ClientAuthenticationMethod.CLIENT_SECRET_BASIC
            "client_secret_post" -> ClientAuthenticationMethod.CLIENT_SECRET_POST
            "none" -> ClientAuthenticationMethod.NONE
            else -> throw IllegalArgumentException("Unsupported client authentication method: ${request.clientAuthenticationMethod}")
        }

        // Map grant types
        val grantTypes = request.authorizationGrantTypes.map { grantType ->
            when (grantType.lowercase()) {
                "authorization_code" -> AuthorizationGrantType.AUTHORIZATION_CODE
                "refresh_token" -> AuthorizationGrantType.REFRESH_TOKEN
                "client_credentials" -> AuthorizationGrantType.CLIENT_CREDENTIALS
                else -> throw IllegalArgumentException("Unsupported grant type: $grantType")
            }
        }.toSet()

        // Build the registered client
        val registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(credentials.clientId)
            .clientSecret(credentials.encodedSecret)
            .clientIdIssuedAt(Instant.now())
            .clientName(request.clientName)
            .clientAuthenticationMethod(clientAuthMethod)
            .apply {
                grantTypes.forEach { authorizationGrantType(it) }
                request.redirectUris.forEach { redirectUri(it) }
                request.postLogoutRedirectUris.forEach { postLogoutRedirectUri(it) }
                request.scopes.forEach { scope(it) }
            }
            .clientSettings(
                ClientSettings.builder()
                    .requireAuthorizationConsent(request.requireAuthorizationConsent)
                    .requireProofKey(request.requireProofKey)
                    .build()
            )
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofSeconds(request.accessTokenTimeToLive))
                    .refreshTokenTimeToLive(Duration.ofSeconds(request.refreshTokenTimeToLive))
                    .reuseRefreshTokens(request.reuseRefreshTokens)
                    .build()
            )
            .build()

        // Save the client
        registeredClientRepository.save(registeredClient)

        return ClientCredentialsDto(
            clientId = credentials.clientId,
            clientSecret = credentials.clientSecret
        )
    }

    /**
     * Get registered client by client ID
     */
    @Transactional(readOnly = true)
    fun getRegisteredClientByClientId(clientId: String): RegisteredClientDto {
        val client = registeredClientRepository.findByClientId(clientId)
            ?: throw IllegalArgumentException("Client not found: $clientId")
        
        return mapToDto(client)
    }

    /**
     * Get registered client by internal ID
     */
    @Transactional(readOnly = true)
    fun getRegisteredClientById(id: String): RegisteredClientDto {
        val client = registeredClientRepository.findById(id)
            ?: throw IllegalArgumentException("Client not found: $id")
        
        return mapToDto(client)
    }

    /**
     * Update a registered client
     */
    fun updateRegisteredClient(clientId: String, request: UpdateRegisteredClientRequest): RegisteredClientDto {
        val existingClient = registeredClientRepository.findByClientId(clientId)
            ?: throw IllegalArgumentException("Client not found: $clientId")

        // Validate scopes if provided
        if (request.scopes != null) {
            val scopeValidation = oauth2ScopeService.validateScopes(request.scopes.toList())
            if (!scopeValidation.isValid) {
                throw IllegalArgumentException("Invalid scopes: ${scopeValidation.invalidScopes.joinToString(", ")}")
            }
        }

        // Build updated client
        val updatedClient = RegisteredClient.from(existingClient)
            .apply {
                request.clientName?.let { clientName(it) }
                request.redirectUris?.let { uris ->
                    redirectUris { it.clear(); it.addAll(uris) }
                }
                request.postLogoutRedirectUris?.let { uris ->
                    postLogoutRedirectUris { it.clear(); it.addAll(uris) }
                }
                request.scopes?.let { scopes ->
                    scopes { it.clear(); it.addAll(scopes) }
                }
            }
            .clientSettings(
                ClientSettings.builder()
                    .requireAuthorizationConsent(request.requireAuthorizationConsent ?: existingClient.clientSettings.isRequireAuthorizationConsent)
                    .requireProofKey(request.requireProofKey ?: existingClient.clientSettings.isRequireProofKey)
                    .build()
            )
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenTimeToLive(
                        request.accessTokenTimeToLive?.let { Duration.ofSeconds(it) }
                            ?: existingClient.tokenSettings.accessTokenTimeToLive
                    )
                    .refreshTokenTimeToLive(
                        request.refreshTokenTimeToLive?.let { Duration.ofSeconds(it) }
                            ?: existingClient.tokenSettings.refreshTokenTimeToLive
                    )
                    .reuseRefreshTokens(request.reuseRefreshTokens ?: existingClient.tokenSettings.isReuseRefreshTokens)
                    .build()
            )
            .build()

        // Save the updated client
        registeredClientRepository.save(updatedClient)
        
        return mapToDto(updatedClient)
    }

    /**
     * Delete a registered client
     */
    fun deleteRegisteredClient(clientId: String) {
        val client = registeredClientRepository.findByClientId(clientId)
            ?: throw IllegalArgumentException("Client not found: $clientId")
        
        // Note: Spring's RegisteredClientRepository doesn't have a delete method by default
        // This would require implementing a custom repository or using JDBC directly
        throw UnsupportedOperationException("Client deletion not supported with current repository implementation")
    }

    /**
     * Generate new client secret for existing client
     */
    fun regenerateClientSecret(clientId: String): ClientCredentialsDto {
        val existingClient = registeredClientRepository.findByClientId(clientId)
            ?: throw IllegalArgumentException("Client not found: $clientId")

        val newSecret = clientSecretService.generateClientSecret()
        val encodedSecret = clientSecretService.encodeSecret(newSecret)

        val updatedClient = RegisteredClient.from(existingClient)
            .clientSecret(encodedSecret)
            .build()

        registeredClientRepository.save(updatedClient)

        return ClientCredentialsDto(
            clientId = clientId,
            clientSecret = newSecret,
            message = "Client secret regenerated successfully. Store the new secret securely - it won't be shown again."
        )
    }

    private fun mapToDto(client: RegisteredClient): RegisteredClientDto {
        return RegisteredClientDto(
            id = client.id,
            clientId = client.clientId,
            clientIdIssuedAt = client.clientIdIssuedAt,
            clientName = client.clientName,
            clientAuthenticationMethods = client.clientAuthenticationMethods.map { it.value }.toSet(),
            authorizationGrantTypes = client.authorizationGrantTypes.map { it.value }.toSet(),
            redirectUris = client.redirectUris,
            postLogoutRedirectUris = client.postLogoutRedirectUris,
            scopes = client.scopes,
            requireAuthorizationConsent = client.clientSettings.isRequireAuthorizationConsent,
            requireProofKey = client.clientSettings.isRequireProofKey,
            accessTokenTimeToLive = client.tokenSettings.accessTokenTimeToLive.seconds,
            refreshTokenTimeToLive = client.tokenSettings.refreshTokenTimeToLive.seconds,
            reuseRefreshTokens = client.tokenSettings.isReuseRefreshTokens
        )
    }
}