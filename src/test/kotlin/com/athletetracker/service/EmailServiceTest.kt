package com.athletetracker.service

import com.athletetracker.config.EmailProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.mail.javamail.JavaMailSender
import java.time.LocalDateTime
import io.mockk.mockk
import io.mockk.every
import org.junit.jupiter.api.Assertions.*

class EmailServiceTest {
    
    private lateinit var emailProperties: EmailProperties
    private lateinit var templateService: MustacheTemplateService
    private lateinit var emailService: EmailService
    private val mailSender: JavaMailSender = mockk()
    
    @BeforeEach
    fun setup() {
        emailProperties = EmailProperties().apply {
            enabled = false  // Test in console mode
            fromAddress = "test@athletetracker.com"
            fromName = "Test Athlete Tracker"
            frontendUrl = "http://localhost:3001"
        }
        
        templateService = MustacheTemplateService()
        emailService = EmailService(emailProperties, templateService, null)
    }
    
    @Test
    fun `should validate email properties correctly`() {
        // Test valid configuration
        val validProps = EmailProperties().apply {
            enabled = true
            fromAddress = "valid@example.com"
            fromName = "Valid Name"
            frontendUrl = "http://example.com"
        }
        
        val validationErrors = validProps.validate()
        assertTrue(validationErrors.isEmpty(), "Valid configuration should have no errors")
        
        // Test invalid configuration
        val invalidProps = EmailProperties().apply {
            enabled = true
            fromAddress = "invalid-email"
            fromName = ""
            frontendUrl = ""
        }
        
        val invalidErrors = invalidProps.validate()
        assertTrue(invalidErrors.isNotEmpty(), "Invalid configuration should have errors")
        assertTrue(invalidErrors.any { it.contains("valid email address") })
        assertTrue(invalidErrors.any { it.contains("from-name") })
        assertTrue(invalidErrors.any { it.contains("frontend-url") })
    }
    
    @Test
    fun `should build invitation URL correctly`() {
        val token = "test-token-123"
        val expectedUrl = "http://localhost:3001/register?token=test-token-123"
        
        val actualUrl = emailProperties.buildInvitationUrl(token)
        assertEquals(expectedUrl, actualUrl)
    }
    
    @Test
    fun `should format sender information correctly`() {
        val expected = "Test Athlete Tracker <test@athletetracker.com>"
        val actual = emailProperties.getFromWithName()
        assertEquals(expected, actual)
    }
    
    @Test
    fun `should send athlete invitation email in console mode`() {
        // This test verifies the email service can process invitation emails
        // In console mode, it should log to console without throwing exceptions
        
        assertDoesNotThrow {
            emailService.sendAthleteInvitation(
                email = "athlete@example.com",
                token = "test-token-123",
                athleteName = "John Doe",
                coachName = "Coach Smith",
                expiresAt = LocalDateTime.now().plusDays(7)
            )
        }
    }
    
    @Test
    fun `should send reminder email in console mode`() {
        // This test verifies the email service can process reminder emails
        // In console mode, it should log to console without throwing exceptions
        
        assertDoesNotThrow {
            emailService.sendInvitationReminder(
                email = "athlete@example.com",
                token = "test-token-123",
                athleteName = "John Doe",
                coachName = "Coach Smith",
                expiresAt = LocalDateTime.now().plusDays(7)
            )
        }
    }
    
    @Test
    fun `should test email configuration successfully`() {
        // This test verifies that the email configuration test passes
        // when email is disabled (console mode)
        
        val result = emailService.testEmailConfiguration()
        assertTrue(result, "Email configuration test should pass in console mode")
    }
}