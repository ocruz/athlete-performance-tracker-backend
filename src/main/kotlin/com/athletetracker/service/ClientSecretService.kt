package com.athletetracker.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

@Service
class ClientSecretService(
    private val passwordEncoder: PasswordEncoder
) {
    
    companion object {
        private const val SECRET_LENGTH = 32
        private val SECURE_RANDOM = SecureRandom()
        private const val CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
    }

    /**
     * Generate a new client secret
     */
    fun generateClientSecret(): String {
        val secret = StringBuilder(SECRET_LENGTH)
        repeat(SECRET_LENGTH) {
            secret.append(CHARSET[SECURE_RANDOM.nextInt(CHARSET.length)])
        }
        return secret.toString()
    }

    /**
     * Generate a cryptographically secure client ID
     */
    fun generateClientId(prefix: String = "client"): String {
        val randomBytes = ByteArray(16)
        SECURE_RANDOM.nextBytes(randomBytes)
        val randomString = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
        return "${prefix}_${randomString}"
    }

    /**
     * Encode (hash) a client secret for secure storage
     */
    fun encodeSecret(rawSecret: String): String {
        return passwordEncoder.encode(rawSecret)
    }

    /**
     * Verify a raw client secret against its encoded (hashed) version
     */
    fun verifySecret(rawSecret: String, encodedSecret: String): Boolean {
        return passwordEncoder.matches(rawSecret, encodedSecret)
    }

    /**
     * Generate both client ID and secret for a new client
     */
    data class ClientCredentials(
        val clientId: String,
        val clientSecret: String,
        val encodedSecret: String
    )

    fun generateClientCredentials(clientPrefix: String = "client"): ClientCredentials {
        val clientId = generateClientId(clientPrefix)
        val clientSecret = generateClientSecret()
        val encodedSecret = encodeSecret(clientSecret)
        
        return ClientCredentials(
            clientId = clientId,
            clientSecret = clientSecret,
            encodedSecret = encodedSecret
        )
    }

    /**
     * Validate client secret strength
     */
    fun validateSecretStrength(secret: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (secret.length < 24) {
            errors.add("Client secret must be at least 24 characters long")
        }

        if (!secret.any { it.isUpperCase() }) {
            errors.add("Client secret must contain at least one uppercase letter")
        }

        if (!secret.any { it.isLowerCase() }) {
            errors.add("Client secret must contain at least one lowercase letter")
        }

        if (!secret.any { it.isDigit() }) {
            errors.add("Client secret must contain at least one digit")
        }

        if (!secret.any { it in "-._~!@#$%^&*" }) {
            errors.add("Client secret should contain at least one special character")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
}