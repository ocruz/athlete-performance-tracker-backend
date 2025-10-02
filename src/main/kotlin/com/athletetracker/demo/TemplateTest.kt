package com.athletetracker.demo

import com.athletetracker.service.MustacheTemplateService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Simple test to verify template loading works
 */
object TemplateTest {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("🧪 TEMPLATE LOADING TEST")
        println("=".repeat(50))
        
        val templateService = MustacheTemplateService()
        
        // Test template existence
        println("📄 Checking template existence...")
        val invitationExists = templateService.templateExists("athlete-invitation")
        val reminderExists = templateService.templateExists("invitation-reminder")
        
        println("✅ Athlete Invitation: ${if (invitationExists) "FOUND" else "NOT FOUND"}")
        println("✅ Invitation Reminder: ${if (reminderExists) "FOUND" else "NOT FOUND"}")
        println()
        
        if (invitationExists) {
            try {
                println("🎨 Testing template rendering...")
                val templateData = mapOf(
                    "subject" to "Test Email",
                    "athleteName" to "Test Athlete",
                    "coachName" to "Test Coach",
                    "invitationUrl" to "http://localhost:3001/register?token=test-123",
                    "expirationDate" to LocalDateTime.now().plusDays(7)
                        .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' h:mm a"))
                )
                
                val renderedHtml = templateService.renderTemplate("athlete-invitation", templateData)
                
                println("✅ Template rendered successfully!")
                println("📝 HTML Preview (first 200 chars):")
                println("-".repeat(50))
                println(renderedHtml.take(200) + "...")
                println("-".repeat(50))
                
            } catch (e: Exception) {
                println("❌ Template rendering failed: ${e.message}")
                e.printStackTrace()
            }
        }
        
        println("\n🏁 Template test completed!")
    }
}