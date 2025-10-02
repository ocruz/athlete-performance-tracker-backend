package com.athletetracker.service

import com.athletetracker.config.EmailProperties
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class EmailService(
    private val emailProperties: EmailProperties,
    private val templateService: MustacheTemplateService,
    private val mailSender: JavaMailSender? = null
) {
    
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    @PostConstruct
    fun initialize() {
        val validationErrors = emailProperties.validate()
        if (validationErrors.isNotEmpty()) {
            logger.warn("Email configuration validation failed: {}", validationErrors.joinToString(", "))
        }
        
        if (emailProperties.enabled) {
            if (mailSender == null) {
                logger.warn("Email is enabled but JavaMailSender is not available. Check your mail configuration.")
            } else {
                logger.info("Email service initialized with SMTP sending enabled")
            }
        } else {
            logger.info("Email service initialized with console logging only")
        }
        
        // Preload templates
        templateService.preloadTemplates()
    }
    
    /**
     * Send athlete invitation email
     */
    fun sendAthleteInvitation(
        email: String, 
        token: String, 
        athleteName: String,
        coachName: String? = null,
        expiresAt: LocalDateTime
    ) {
        val templateData = mutableMapOf<String, Any>(
            "subject" to "Welcome to Athlete Performance Tracker - Create Your Account",
            "athleteName" to athleteName,
            "invitationUrl" to emailProperties.buildInvitationUrl(token),
            "expirationDate" to expiresAt.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' h:mm a"))
        ).apply {
            if (coachName != null) {
                put("coachName", coachName)
            }
        }
        
        sendTemplatedEmail(
            to = email,
            subject = templateData["subject"] as String,
            templateName = "athlete-invitation",
            templateData = templateData
        )
    }
    
    /**
     * Send invitation reminder email
     */
    fun sendInvitationReminder(
        email: String,
        token: String,
        athleteName: String,
        coachName: String? = null,
        expiresAt: LocalDateTime
    ) {
        val templateData = mutableMapOf<String, Any>(
            "subject" to "Reminder: Complete Your Athlete Performance Tracker Registration",
            "athleteName" to athleteName,
            "invitationUrl" to emailProperties.buildInvitationUrl(token),
            "expirationDate" to expiresAt.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' h:mm a"))
        ).apply {
            if (coachName != null) {
                put("coachName", coachName)
            }
        }
        
        sendTemplatedEmail(
            to = email,
            subject = templateData["subject"] as String,
            templateName = "invitation-reminder",
            templateData = templateData
        )
    }
    
    /**
     * Send templated email
     */
    private fun sendTemplatedEmail(
        to: String,
        subject: String,
        templateName: String,
        templateData: Map<String, Any>
    ) {
        try {
            val emailContent = templateService.renderTemplate(templateName, templateData)
            
            if (emailProperties.enabled && mailSender != null) {
                sendEmailViaSMTP(to, subject, emailContent)
            } else {
                logEmailToConsole(to, subject, emailContent)
            }
        } catch (e: Exception) {
            logger.error("Failed to send email to: $to with template: $templateName", e)
            throw RuntimeException("Email sending failed", e)
        }
    }
    
    /**
     * Send email via SMTP
     */
    private fun sendEmailViaSMTP(to: String, subject: String, htmlContent: String) {
        try {
            val mimeMessage = mailSender!!.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")
            
            helper.setFrom(emailProperties.getFromWithName())
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlContent, true) // true = HTML content
            
            mailSender.send(mimeMessage)
            logger.info("Email sent successfully to: $to")
        } catch (e: Exception) {
            logger.error("Failed to send email via SMTP to: $to", e)
            throw e
        }
    }
    
    /**
     * Log email to console for development
     */
    private fun logEmailToConsole(to: String, subject: String, content: String) {
        println("\n" + "=".repeat(80))
        println("📧 EMAIL (Console Mode)")
        println("=".repeat(80))
        println("From: ${emailProperties.getFromWithName()}")
        println("To: $to")
        println("Subject: $subject")
        println("-".repeat(80))
        println(content)
        println("=".repeat(80))
        println()
        
        logger.debug("Email logged to console for: $to")
    }
    
    /**
     * Test email configuration
     */
    fun testEmailConfiguration(): Boolean {
        return try {
            if (!emailProperties.enabled) {
                logger.info("Email testing skipped - email is disabled")
                return true
            }
            
            if (mailSender == null) {
                logger.error("Email testing failed - JavaMailSender not available")
                return false
            }
            
            // Test template rendering
            val testData = mapOf(
                "subject" to "Test Email",
                "athleteName" to "Test Athlete",
                "invitationUrl" to "https://example.com/test",
                "expirationDate" to "December 31, 2024 at 11:59 PM"
            )
            
            templateService.renderTemplate("athlete-invitation", testData)
            logger.info("Email configuration test passed")
            true
        } catch (e: Exception) {
            logger.error("Email configuration test failed", e)
            false
        }
    }
    
}