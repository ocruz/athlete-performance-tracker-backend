package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.User
import com.athletetracker.entity.UserRole
import com.athletetracker.repository.UserRepository
import com.athletetracker.security.JwtService
import com.athletetracker.service.UserRoleService
import com.athletetracker.service.AthleteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import java.time.LocalDateTime

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val userRoleService: UserRoleService,
    private val athleteService: AthleteService
) {

    @PostMapping("/oauth2/login")
    fun oauth2Login(
        @Valid @RequestBody loginRequest: LoginRequest,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.email,
                    loginRequest.password
                )
            )

            val user = authentication.principal as User
            
            // Update last login time
            val updatedUser = user.copy(lastLoginAt = LocalDateTime.now())
            userRepository.save(updatedUser)

            // Set authentication details for the session
            val authToken = UsernamePasswordAuthenticationToken(
                updatedUser, 
                null, 
                authentication.authorities
            )
            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)

            // Store authentication in security context for this session
            SecurityContextHolder.getContext().authentication = authToken
            
            // Save the security context in the session
            val session = request.getSession(true)
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                SecurityContextHolder.getContext())

            // Return success response (no JWT tokens for OAuth2 flow)
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Authentication successful"
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf(
                "success" to false,
                "message" to "Invalid email or password"
            ))
        }
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.email,
                    loginRequest.password
                )
            )

            val user = authentication.principal as User
            
            // Update last login time
            val updatedUser = user.copy(lastLoginAt = LocalDateTime.now())
            userRepository.save(updatedUser)

            // Determine profile IDs based on user role
            val extraClaims = mutableMapOf<String, Any>()
            
            when (user.role) {
                UserRole.ATHLETE -> {
                    // Find the athlete record linked to this user
                    val athlete = athleteService.findAthleteByUserId(user.id)
                    if (athlete != null) {
                        extraClaims["athleteId"] = athlete.id
                    }
                    // Don't add coachId for athletes
                }
                UserRole.COACH, UserRole.ADMIN -> {
                    // For coaches and admins, use the user ID as coach ID
                    extraClaims["coachId"] = user.id
                    // Don't add athleteId for coaches/admins
                }
            }

            val token = jwtService.generateToken(extraClaims, user)
            val refreshToken = jwtService.generateRefreshToken(user)

            val response = LoginResponse(
                token = token,
                refreshToken = refreshToken,
                user = updatedUser.toDto(),
                expiresIn = jwtService.getExpirationTime(),
                defaultRoute = userRoleService.getDefaultRouteForUser(updatedUser)
            )

            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        try {
            val userEmail = jwtService.extractUsername(request.refreshToken)
            val user = userRepository.findByEmail(userEmail)
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

            if (jwtService.isTokenValid(request.refreshToken, user)) {
                // Include profile IDs in refresh token as well
                val extraClaims = mutableMapOf<String, Any?>()
                
                when (user.role) {
                    UserRole.ATHLETE -> {
                        val athlete = athleteService.findAthleteByUserId(user.id)
                        if (athlete != null) {
                            extraClaims["athleteId"] = athlete.id
                        }
                        extraClaims["coachId"] = null
                    }
                    UserRole.COACH, UserRole.ADMIN -> {
                        extraClaims["coachId"] = user.id
                        extraClaims["athleteId"] = null
                    }
                }
                
                val newToken = jwtService.generateToken(extraClaims, user)
                val newRefreshToken = jwtService.generateRefreshToken(user)

                val response = RefreshTokenResponse(
                    token = newToken,
                    refreshToken = newRefreshToken,
                    expiresIn = jwtService.getExpirationTime()
                )

                return ResponseEntity.ok(response)
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserDto> {
        val user = authentication.principal as User
        return ResponseEntity.ok(user.toDto())
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        authentication: Authentication
    ): ResponseEntity<String> {
        val user = authentication.principal as User

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            return ResponseEntity.badRequest().body("Current password is incorrect")
        }

        val updatedUser = user.copy(password = passwordEncoder.encode(request.newPassword))
        userRepository.save(updatedUser)

        return ResponseEntity.ok("Password changed successfully")
    }

    @PutMapping("/profile")
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequest,
        authentication: Authentication
    ): ResponseEntity<UserDto> {
        val user = authentication.principal as User

        val updatedUser = user.copy(
            firstName = request.firstName,
            lastName = request.lastName,
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(updatedUser)
        return ResponseEntity.ok(savedUser.toDto())
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<String> {
        // With stateless JWT, logout is handled on the client side
        // Optionally, you could implement a token blacklist here
        return ResponseEntity.ok("Logged out successfully")
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Map<String, String>> {
        val errorResponse = mapOf(
            "error" to "Authentication failed",
            "message" to (e.message ?: "Unknown error occurred")
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }
}