package com.athletetracker.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import java.util.function.Function
import javax.crypto.SecretKey
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Service
class JwtService {

    @Value("\${app.jwt.secret:your-secret-key-that-is-at-least-256-bits-long-for-hs256}")
    private lateinit var secretKey: String

    @Value("\${app.jwt.expiration:86400000}") // 24 hours
    private var jwtExpiration: Long = 86400000

    @Value("\${app.jwt.refresh-expiration:604800000}") // 7 days
    private var refreshExpiration: Long = 604800000

    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun extractRole(token: String): String? {
        return extractClaim(token) { claims -> claims["role"] as? String }
    }

    fun extractAthleteId(token: String): Long? {
        return extractClaim(token) { claims -> 
            val athleteId = claims["athleteId"]
            when (athleteId) {
                is Number -> athleteId.toLong()
                is String -> athleteId.toLongOrNull()
                else -> null
            }
        }
    }

    fun extractCoachId(token: String): Long? {
        return extractClaim(token) { claims -> 
            val coachId = claims["coachId"]
            when (coachId) {
                is Number -> coachId.toLong()
                is String -> coachId.toLongOrNull()
                else -> null
            }
        }
    }

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    fun generateToken(userDetails: UserDetails): String {
        return generateToken(HashMap(), userDetails)
    }

    fun generateToken(
        extraClaims: Map<String, Any?>,
        userDetails: UserDetails
    ): String {
        val authorities = userDetails.authorities.map { it.authority }
        val role = authorities.firstOrNull()?.removePrefix("ROLE_")
        
        val claims = HashMap(extraClaims).apply {
            put("role", role)
            put("authorities", authorities)
        }
        
        return buildToken(claims, userDetails, jwtExpiration)
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return buildToken(HashMap(), userDetails, refreshExpiration)
    }

    private fun buildToken(
        extraClaims: Map<String, Any?>,
        userDetails: UserDetails,
        expiration: Long
    ): String {
        return Jwts
            .builder()
            .setClaims(extraClaims.filterValues { it != null })
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username) && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts
            .parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun getSignInKey(): SecretKey {
        val keyBytes = secretKey.toByteArray()
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun getExpirationTime(): Long = jwtExpiration
    
    fun getRefreshExpirationTime(): Long = refreshExpiration
}