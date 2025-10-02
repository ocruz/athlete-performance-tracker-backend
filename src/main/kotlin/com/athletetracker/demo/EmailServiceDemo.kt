package com.athletetracker.demo

import com.athletetracker.config.EmailProperties
import com.athletetracker.service.MustacheTemplateService
import com.athletetracker.service.EmailService
import java.time.LocalDateTime

/**
 * Simple demo to show EmailService functionality
 * Run this as a standalone class
 */
object EmailServiceDemo {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("=".repeat(80))
        println("🚀 EMAIL SERVICE DEMO")
        println("=".repeat(80))
        println()
        
        // Create email properties for demo
        val emailProperties = EmailProperties().apply {
            enabled = false  // Keep in console mode for demo
            fromAddress = "demo@athletetracker.com"
            fromName = "Athlete Performance Tracker Demo"
            frontendUrl = "http://localhost:3001"
        }
        
        // Create template service
        val templateService = MustacheTemplateService()
        
        // Create email service (no mail sender for console mode)
        val emailService = EmailService(emailProperties, templateService, null)
        
        try {
            // Test email configuration
            println("📋 Testing Email Configuration...")
            val configTest = emailService.testEmailConfiguration()
            println("✅ Configuration Test: ${if (configTest) "PASSED" else "FAILED"}")
            println()
            
            // Test template existence
            println("📄 Checking Template Availability...")
            val invitationTemplateExists = templateService.templateExists("athlete-invitation")
            val reminderTemplateExists = templateService.templateExists("invitation-reminder")
            
            println("✅ Athlete Invitation Template: ${if (invitationTemplateExists) "FOUND" else "NOT FOUND"}")
            println("✅ Invitation Reminder Template: ${if (reminderTemplateExists) "FOUND" else "NOT FOUND"}")
            println()
            
            if (invitationTemplateExists) {
                println("📧 Demo: Sending Athlete Invitation Email...")
                println("-".repeat(60))
                
                emailService.sendAthleteInvitation(
                    email = "john.doe@example.com",
                    token = "demo-invitation-token-12345",
                    athleteName = "John Doe",
                    coachName = "Coach Sarah Wilson",
                    expiresAt = LocalDateTime.now().plusDays(7)
                )
                
                println("✅ Invitation email processed successfully!")
                println()
            }
            
            if (reminderTemplateExists) {
                println("📧 Demo: Sending Invitation Reminder Email...")
                println("-".repeat(60))
                
                emailService.sendInvitationReminder(
                    email = "jane.smith@example.com",
                    token = "demo-reminder-token-67890",
                    athleteName = "Jane Smith",
                    coachName = null, // No coach name for this demo
                    expiresAt = LocalDateTime.now().plusDays(3)
                )
                
                println("✅ Reminder email processed successfully!")
                println()
            }
            
            // Show email properties
            println("🔧 Email Configuration:")
            println("From: ${emailProperties.getFromWithName()}")
            println("Frontend URL: ${emailProperties.frontendUrl}")
            println("SMTP Enabled: ${emailProperties.enabled}")
            println()
            
            // Template demo URLs
            val demoToken = "sample-token-123"
            val demoUrl = emailProperties.buildInvitationUrl(demoToken)
            println("🔗 Sample Invitation URL: $demoUrl")
            println()
            
            println("🎉 EMAIL SERVICE DEMO COMPLETED SUCCESSFULLY!")
            println("=".repeat(80))
            
        } catch (e: Exception) {
            println("❌ Demo failed with error: ${e.message}")
            e.printStackTrace()
        }
    }
}