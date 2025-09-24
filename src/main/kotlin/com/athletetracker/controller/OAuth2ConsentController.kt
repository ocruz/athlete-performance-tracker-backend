package com.athletetracker.controller

import com.athletetracker.dto.UserConsentDto
import com.athletetracker.entity.User
import com.athletetracker.service.ConsentHelperService
import com.athletetracker.service.OAuth2ScopeService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Controller
class OAuth2ConsentController(
    private val registeredClientRepository: RegisteredClientRepository,
    private val authorizationConsentService: OAuth2AuthorizationConsentService,
    private val oauth2ScopeService: OAuth2ScopeService,
    private val consentHelperService: ConsentHelperService
) {

    /**
     * Display OAuth2 consent page
     */
    @GetMapping("/oauth2/consent")
    fun consent(
        principal: Principal,
        model: Model,
        @RequestParam(OAuth2ParameterNames.CLIENT_ID) clientId: String,
        @RequestParam(OAuth2ParameterNames.SCOPE) scope: String,
        @RequestParam(OAuth2ParameterNames.STATE) state: String,
        @RequestParam(name = "user_code", required = false) userCode: String?,
        request: HttpServletRequest
    ): String {
        
        // Get the authorization from the session or state
        val authorizationConsent = authorizationConsentService.findById(clientId, principal.name)
        val previouslyApprovedScopes = authorizationConsent?.authorities?.map { 
            it.authority.removePrefix("SCOPE_") 
        }?.toSet() ?: emptySet()

        // Parse requested scopes
        val requestedScopes = StringUtils.delimitedListToStringArray(scope, " ").toList()
        val scopesToApprove = requestedScopes.filterNot { it in previouslyApprovedScopes }

        if (scopesToApprove.isEmpty()) {
            // All scopes were previously approved
            return "redirect:/oauth2/authorize?" + request.queryString
        }

        // Get registered client
        val registeredClient = registeredClientRepository.findByClientId(clientId)
            ?: throw IllegalArgumentException("Invalid client ID: $clientId")

        // Get detailed information about requested scopes
        val scopeDescriptions = oauth2ScopeService.getScopeDescriptions(scopesToApprove)
        val scopesByCategory = scopesToApprove.groupBy { 
            scopeDescriptions[it]?.category ?: "OTHER" 
        }

        // Prepare consent page model
        val consentPageModel = ConsentPageModel(
            clientId = clientId,
            clientName = registeredClient.clientName,
            state = state,
            userCode = userCode,
            scopes = scopesToApprove,
            scopeDescriptions = scopeDescriptions,
            scopesByCategory = scopesByCategory,
            sensitiveScopes = scopesToApprove.filter { 
                scopeDescriptions[it]?.isSensitive == true 
            }
        )

        model.addAttribute("consent", consentPageModel)
        model.addAttribute("principalName", principal.name)
        
        return "oauth2/consent"
    }

    /**
     * Handle consent form submission
     */
    @PostMapping("/oauth2/consent")
    fun processConsent(
        principal: Principal,
        @RequestParam(OAuth2ParameterNames.CLIENT_ID) clientId: String,
        @RequestParam(OAuth2ParameterNames.STATE) state: String,
        @RequestParam(name = "user_code", required = false) userCode: String?,
        @RequestParam(name = "approved_scopes", required = false) approvedScopes: List<String>?,
        @RequestParam(name = "action") action: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String {

        if ("approve" != action) {
            // User denied consent
            return buildRedirectUri(request, "access_denied", "User denied consent")
        }

        val finalApprovedScopes = approvedScopes ?: emptyList()
        
        // Save user consent
        consentHelperService.saveConsent(
            clientId = clientId,
            principalName = principal.name,
            approvedScopes = finalApprovedScopes
        )

        // Redirect back to authorization endpoint with approved scopes
        val queryString = request.queryString
        val updatedQuery = updateScopeParameter(queryString, finalApprovedScopes.joinToString(" "))
        
        return "redirect:/oauth2/authorize?$updatedQuery"
    }

    /**
     * API endpoint to get user's consent history
     */
    @GetMapping("/api/oauth2/consents")
    @ResponseBody
    fun getUserConsents(authentication: Authentication): List<UserConsentDto> {
        val user = authentication.principal as User
        return consentHelperService.getUserConsents(user.email)
    }

    /**
     * API endpoint to revoke consent for a specific client
     */
    @DeleteMapping("/api/oauth2/consents/{clientId}")
    @ResponseBody
    fun revokeConsent(
        @PathVariable clientId: String,
        authentication: Authentication
    ): Map<String, String> {
        val user = authentication.principal as User
        consentHelperService.revokeConsent(clientId, user.email)
        return mapOf("message" to "Consent revoked successfully")
    }

    private fun buildRedirectUri(request: HttpServletRequest, error: String, errorDescription: String): String {
        val redirectUri = request.getParameter("redirect_uri")
        val state = request.getParameter("state")
        
        val params = mutableMapOf<String, String>()
        params["error"] = error
        params["error_description"] = errorDescription
        if (state != null) {
            params["state"] = state
        }
        
        val queryString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "redirect:$redirectUri?$queryString"
    }

    private fun updateScopeParameter(queryString: String, newScope: String): String {
        val params = queryString.split("&").toMutableList()
        params.removeIf { it.startsWith("scope=") }
        params.add("scope=${newScope.replace(" ", "+")}")
        return params.joinToString("&")
    }

    data class ConsentPageModel(
        val clientId: String,
        val clientName: String,
        val state: String,
        val userCode: String?,
        val scopes: List<String>,
        val scopeDescriptions: Map<String, OAuth2ScopeService.ScopeDescription>,
        val scopesByCategory: Map<String, List<String>>,
        val sensitiveScopes: List<String>
    )
}