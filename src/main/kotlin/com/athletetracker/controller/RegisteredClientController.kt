package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.service.OAuth2ScopeService
import com.athletetracker.service.RegisteredClientService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/oauth2/clients")
@CrossOrigin(origins = ["http://localhost:3001"])
@PreAuthorize("hasRole('ADMIN')")
class RegisteredClientController(
    private val registeredClientService: RegisteredClientService,
    private val oauth2ScopeService: OAuth2ScopeService
) {

    /**
     * Create a new OAuth2 client
     */
    @PostMapping
    fun createClient(@Valid @RequestBody request: CreateRegisteredClientRequest): ResponseEntity<ClientCredentialsDto> {
        val credentials = registeredClientService.createRegisteredClient(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(credentials)
    }

    /**
     * Get client by client ID
     */
    @GetMapping("/client/{clientId}")
    fun getClientByClientId(@PathVariable clientId: String): ResponseEntity<RegisteredClientDto> {
        val client = registeredClientService.getRegisteredClientByClientId(clientId)
        return ResponseEntity.ok(client)
    }

    /**
     * Get client by internal ID
     */
    @GetMapping("/{id}")
    fun getClientById(@PathVariable id: String): ResponseEntity<RegisteredClientDto> {
        val client = registeredClientService.getRegisteredClientById(id)
        return ResponseEntity.ok(client)
    }

    /**
     * Update a client
     */
    @PutMapping("/client/{clientId}")
    fun updateClient(
        @PathVariable clientId: String,
        @Valid @RequestBody request: UpdateRegisteredClientRequest
    ): ResponseEntity<RegisteredClientDto> {
        val updatedClient = registeredClientService.updateRegisteredClient(clientId, request)
        return ResponseEntity.ok(updatedClient)
    }

    /**
     * Regenerate client secret
     */
    @PostMapping("/client/{clientId}/regenerate-secret")
    fun regenerateClientSecret(@PathVariable clientId: String): ResponseEntity<ClientCredentialsDto> {
        val credentials = registeredClientService.regenerateClientSecret(clientId)
        return ResponseEntity.ok(credentials)
    }

    /**
     * Get available OAuth2 scopes
     */
    @GetMapping("/scopes")
    fun getAvailableScopes(): ResponseEntity<AvailableScopesResponse> {
        val scopes = oauth2ScopeService.getAllScopes()
        val scopesByCategory = oauth2ScopeService.getScopesByCategory()
        
        val response = AvailableScopesResponse(
            scopes = scopes,
            categories = scopesByCategory
        )
        
        return ResponseEntity.ok(response)
    }

    /**
     * Get default scopes (useful for client registration UI)
     */
    @GetMapping("/scopes/default")
    fun getDefaultScopes(): ResponseEntity<List<OAuth2ScopeDto>> {
        val defaultScopes = oauth2ScopeService.getDefaultScopes()
        return ResponseEntity.ok(defaultScopes)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
    }

    @ExceptionHandler(UnsupportedOperationException::class)
    fun handleUnsupportedOperationException(e: UnsupportedOperationException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Operation not supported")))
    }
}