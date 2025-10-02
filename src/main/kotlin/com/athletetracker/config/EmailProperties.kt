package com.athletetracker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.email")
data class EmailProperties(
    var enabled: Boolean = false,
    var fromAddress: String = "noreply@athletetracker.com",
    var fromName: String = "Athlete Performance Tracker",
    var frontendUrl: String = "http://localhost:3001",
    var templatePath: String = "classpath:/templates/email/"
) {
    
    /**
     * Get the full sender information for email headers
     */
    fun getFromWithName(): String {
        return "$fromName <$fromAddress>"
    }
    
    /**
     * Build invitation URL with token
     */
    fun buildInvitationUrl(token: String): String {
        return "$frontendUrl/register?token=$token"
    }
    
    /**
     * Validate configuration
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (enabled) {
            if (fromAddress.isBlank()) {
                errors.add("app.email.from-address cannot be blank when email is enabled")
            }
            if (fromName.isBlank()) {
                errors.add("app.email.from-name cannot be blank when email is enabled")
            }
            if (frontendUrl.isBlank()) {
                errors.add("app.email.frontend-url cannot be blank when email is enabled")
            }
            if (!fromAddress.contains("@")) {
                errors.add("app.email.from-address must be a valid email address")
            }
        }
        
        return errors
    }
}