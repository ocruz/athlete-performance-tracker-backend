package com.athletetracker.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class FrontendLoginRedirectEntryPoint(
    @Value("\${app.oauth2.frontend-login-url:http://localhost:3001/login}")
    private val frontendLoginUrl: String
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        // Build the complete OAuth2 authorization URL that the user was trying to access
        val requestUrl = buildOriginalRequestUrl(request)
        
        // URL encode the return URL for safe passing as query parameter
        val encodedReturnUrl = URLEncoder.encode(requestUrl, StandardCharsets.UTF_8.toString())
        
        // Build the frontend login URL with the return URL
        val loginUrl = if (frontendLoginUrl.contains("?")) {
            "$frontendLoginUrl&returnUrl=$encodedReturnUrl"
        } else {
            "$frontendLoginUrl?returnUrl=$encodedReturnUrl"
        }
        
        // Redirect to frontend login page
        response.sendRedirect(loginUrl)
    }

    /**
     * Reconstructs the original OAuth2 authorization request URL including all query parameters
     */
    private fun buildOriginalRequestUrl(request: HttpServletRequest): String {
        val requestUrl = StringBuilder()
        
        // Add the base URL (scheme, host, port, and path)
        requestUrl.append(request.scheme)
            .append("://")
            .append(request.serverName)
        
        // Add port if it's not the default port
        if (request.serverPort != 80 && request.serverPort != 443) {
            requestUrl.append(":").append(request.serverPort)
        }
        
        // Add the context path and request URI
        requestUrl.append(request.contextPath)
            .append(request.servletPath)
        
        // Add path info if present
        if (request.pathInfo != null) {
            requestUrl.append(request.pathInfo)
        }
        
        // Add query string if present (this contains the OAuth2 parameters)
        if (request.queryString != null) {
            requestUrl.append("?").append(request.queryString)
        }
        
        return requestUrl.toString()
    }
}