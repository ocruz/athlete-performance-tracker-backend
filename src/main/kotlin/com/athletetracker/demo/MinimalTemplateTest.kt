package com.athletetracker.demo

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheResolver
import org.springframework.core.io.ClassPathResource
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter

object MinimalTemplateTest {
    
    class EmailMustacheResolver : MustacheResolver {
        override fun getReader(resourceName: String): Reader? {
            return try {
                val templatePath = when {
                    resourceName.startsWith("templates/email/") -> resourceName
                    resourceName.startsWith("common/") -> "templates/email/$resourceName"
                    else -> "templates/email/$resourceName"
                }
                
                val resource = ClassPathResource("$templatePath.mustache")
                if (resource.exists()) {
                    println("📄 Loading template: $templatePath")
                    InputStreamReader(resource.inputStream, Charsets.UTF_8)
                } else {
                    println("❌ Template not found: $templatePath")
                    null
                }
            } catch (e: Exception) {
                println("❌ Error loading template: $resourceName - ${e.message}")
                null
            }
        }
    }
    
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            println("🚀 Starting minimal template test...")
            
            val factory = DefaultMustacheFactory(EmailMustacheResolver())
            
            println("📋 Testing template existence...")
            
            // Test header template
            val headerMustache = factory.compile("common/header")
            println("✅ Header template loaded")
            
            // Test footer template  
            val footerMustache = factory.compile("common/footer")
            println("✅ Footer template loaded")
            
            // Test main invitation template
            val invitationMustache = factory.compile("athlete-invitation")
            println("✅ Invitation template loaded")
            
            // Test rendering with sample data
            println("🎨 Testing template rendering...")
            val templateData = mapOf(
                "subject" to "Test Subject",
                "athleteName" to "John Doe",
                "coachName" to "Coach Smith",
                "invitationUrl" to "http://localhost:3001/register?token=test123",
                "expirationDate" to "December 31, 2024 at 11:59 PM",
                "currentYear" to 2024
            )
            
            val writer = StringWriter()
            invitationMustache.execute(writer, templateData)
            val result = writer.toString()
            
            println("✅ Template rendered successfully!")
            println("📏 HTML length: ${result.length} characters")
            println("🔍 Contains expected elements:")
            println("   - HTML DOCTYPE: ${result.contains("<!DOCTYPE html")}")
            println("   - Athlete name: ${result.contains("John Doe")}")
            println("   - Coach name: ${result.contains("Coach Smith")}")
            println("   - Invitation URL: ${result.contains("http://localhost:3001")}")
            
            if (result.length > 1000 && result.contains("John Doe")) {
                println("🎉 SUCCESS: Template system working correctly!")
                System.exit(0)
            } else {
                println("❌ FAILURE: Template output seems incomplete")
                System.exit(1)
            }
            
        } catch (e: Exception) {
            println("❌ FAILURE: ${e.message}")
            e.printStackTrace()
            System.exit(1)
        }
    }
}
