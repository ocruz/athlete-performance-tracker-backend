package com.athletetracker.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class EmailService(
    @Value("\${app.frontend-url:http://localhost:3000}") private val frontendUrl: String
) {
    
    /**
     * Send athlete invitation email
     * For MVP: Logs invitation details to console
     * TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
     */
    fun sendAthleteInvitation(
        email: String, 
        token: String, 
        athleteName: String,
        coachName: String? = null,
        expiresAt: LocalDateTime
    ) {
        val invitationUrl = "$frontendUrl/register?token=$token"
        val expirationDate = expiresAt.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' h:mm a"))
        
        // For development: Log the invitation details
        logInvitationEmail(email, athleteName, coachName, invitationUrl, expirationDate)
        
        // TODO: Replace with actual email sending
        // emailProvider.send(buildInvitationEmail(email, athleteName, coachName, invitationUrl, expirationDate))
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
        val invitationUrl = "$frontendUrl/register?token=$token"
        val expirationDate = expiresAt.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' h:mm a"))
        
        logReminderEmail(email, athleteName, coachName, invitationUrl, expirationDate)
        
        // TODO: Replace with actual email sending
    }
    
    private fun logInvitationEmail(
        email: String,
        athleteName: String,
        coachName: String?,
        invitationUrl: String,
        expirationDate: String
    ) {
        println("\n" + "=".repeat(60))
        println("📧 ATHLETE INVITATION EMAIL")
        println("=".repeat(60))
        println("To: $email")
        println("Subject: Welcome to Athlete Performance Tracker - Create Your Account")
        println()
        println("Dear $athleteName,")
        println()
        if (coachName != null) {
            println("$coachName has created a performance profile for you in our")
        } else {
            println("A performance profile has been created for you in our")
        }
        println("Athlete Performance Tracker system.")
        println()
        println("To access your profile and start tracking your performance,")
        println("please create your account by clicking the link below:")
        println()
        println("🔗 $invitationUrl")
        println()
        println("This invitation will expire on $expirationDate")
        println()
        println("If you have any questions, please contact your coach or")
        println("the facility administrator.")
        println()
        println("Best regards,")
        println("Athlete Performance Tracker Team")
        println("=".repeat(60))
        println()
    }
    
    private fun logReminderEmail(
        email: String,
        athleteName: String,
        coachName: String?,
        invitationUrl: String,
        expirationDate: String
    ) {
        println("\n" + "=".repeat(60))
        println("📧 INVITATION REMINDER EMAIL")
        println("=".repeat(60))
        println("To: $email")
        println("Subject: Reminder: Complete Your Athlete Performance Tracker Registration")
        println()
        println("Dear $athleteName,")
        println()
        println("This is a friendly reminder that you have a pending invitation")
        println("to create your Athlete Performance Tracker account.")
        println()
        println("Your registration link: $invitationUrl")
        println()
        println("This invitation will expire on $expirationDate")
        println()
        println("Don't miss out on tracking your performance progress!")
        println()
        println("Best regards,")
        println("Athlete Performance Tracker Team")
        println("=".repeat(60))
        println()
    }
    
    /**
     * Template for actual email implementation
     */
    private fun buildInvitationEmailHtml(
        athleteName: String,
        coachName: String?,
        invitationUrl: String,
        expirationDate: String
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome to Athlete Performance Tracker</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #000; text-align: center;">Welcome to Athlete Performance Tracker</h1>
                    
                    <p>Dear $athleteName,</p>
                    
                    <p>${if (coachName != null) "$coachName has created" else "A"} performance profile has been created for you in our Athlete Performance Tracker system.</p>
                    
                    <p>To access your profile and start tracking your performance, please create your account by clicking the button below:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="$invitationUrl" style="background-color: #000; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">Create My Account</a>
                    </div>
                    
                    <p><small>Or copy and paste this link into your browser: <a href="$invitationUrl">$invitationUrl</a></small></p>
                    
                    <p><strong>Important:</strong> This invitation will expire on $expirationDate</p>
                    
                    <p>If you have any questions, please contact your coach or the facility administrator.</p>
                    
                    <p>Best regards,<br>Athlete Performance Tracker Team</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}