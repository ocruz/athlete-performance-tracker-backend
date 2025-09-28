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
    private val athleteService: AthleteService,
    private val invitationService: com.athletetracker.service.InvitationService
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

    // Registration endpoints for athlete invitation system

    @PostMapping("/register/validate-invitation")
    fun validateInvitation(@RequestParam token: String): ResponseEntity<InvitationValidationResponse> {
        try {
            val invitation = invitationService.validateInvitation(token)
            
            if (invitation == null) {
                return ResponseEntity.ok(InvitationValidationResponse(
                    isValid = false,
                    message = "Invalid or expired invitation token"
                ))
            }
            
            val athlete = athleteService.getAthleteEntityById(invitation.athleteId)
            
            return ResponseEntity.ok(InvitationValidationResponse(
                isValid = true,
                athleteInfo = AthletePreviewDto(
                    firstName = athlete.firstName,
                    lastName = athlete.lastName,
                    email = athlete.email,
                    sport = athlete.sport.name,
                    position = athlete.position
                )
            ))
        } catch (e: Exception) {
            return ResponseEntity.ok(InvitationValidationResponse(
                isValid = false,
                message = "Error validating invitation: ${e.message}"
            ))
        }
    }

    @PostMapping("/register/complete")
    fun completeRegistration(@Valid @RequestBody request: CompleteRegistrationRequest): ResponseEntity<RegistrationResponse> {
        try {
            // Validate passwords match
            if (request.password != request.confirmPassword) {
                return ResponseEntity.badRequest().body(RegistrationResponse(
                    success = false,
                    message = "Passwords do not match"
                ))
            }
            
            // Validate invitation
            val invitation = invitationService.validateInvitation(request.token)
                ?: return ResponseEntity.badRequest().body(RegistrationResponse(
                    success = false,
                    message = "Invalid or expired invitation"
                ))
            
            val athlete = athleteService.getAthleteEntityById(invitation.athleteId)
            
            // Check if athlete already has a user account
            if (athlete.userId != null) {
                return ResponseEntity.badRequest().body(RegistrationResponse(
                    success = false,
                    message = "This athlete already has an account"
                ))
            }
            
            // Check if email is already registered
            if (userRepository.existsByEmail(athlete.email!!)) {
                return ResponseEntity.badRequest().body(RegistrationResponse(
                    success = false,
                    message = "An account with this email already exists"
                ))
            }
            
            // Create user account
            val user = User(
                firstName = athlete.firstName,
                lastName = athlete.lastName,
                email = athlete.email,
                password = passwordEncoder.encode(request.password),
                role = UserRole.ATHLETE,
                isActive = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            val savedUser = userRepository.save(user)
            
            // Link athlete to user
            athleteService.linkAthleteToUser(athlete.id, savedUser.id)
            
            // Mark invitation as used
            invitationService.markInvitationAsUsed(invitation.id)
            
            // Generate JWT token
            val token = jwtService.generateToken(savedUser)
            
            return ResponseEntity.ok(RegistrationResponse(
                success = true,
                token = token,
                user = savedUser.toDto(),
                athlete = AthleteBasicDto(
                    id = athlete.id,
                    firstName = athlete.firstName,
                    lastName = athlete.lastName,
                    fullName = athlete.fullName,
                    sport = athlete.sport.name,
                    position = athlete.position,
                    dateOfBirth = athlete.dateOfBirth,
                    email = athlete.email
                ),
                message = "Account created successfully"
            ))
            
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RegistrationResponse(
                success = false,
                message = "Error creating account: ${e.message}"
            ))
        }
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