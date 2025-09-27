package com.athletetracker.util

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/**
 * Utility to generate BCrypt password hashes for development/testing
 */
fun main() {
    val encoder = BCryptPasswordEncoder(12)
    val password = "prospecto-secret"
    val hash = encoder.encode(password)
    
    println("Password: $password")
    println("BCrypt Hash (strength 12): $hash")
    println()
    println("SQL Update Statement:")
    println("UPDATE users SET password = '$hash' WHERE email = 'your-email@example.com';")
    println()
    
    // Test the hash
    val matches = encoder.matches(password, hash)
    println("Hash verification test: $matches")
}