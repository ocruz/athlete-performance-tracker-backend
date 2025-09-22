package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "client_applications")
data class ClientApplication(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "client_id", unique = true, nullable = false)
    val clientId: String,
    
    @Column(name = "client_name", nullable = false)
    val clientName: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(name = "logo_url")
    val logoUrl: String? = null,
    
    @Column(name = "website_url")
    val websiteUrl: String? = null,
    
    @Column(name = "privacy_policy_url")
    val privacyPolicyUrl: String? = null,
    
    @Column(name = "terms_of_service_url")
    val termsOfServiceUrl: String? = null,
    
    @Column(name = "contact_email")
    val contactEmail: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false)
    val clientType: ClientType = ClientType.CONFIDENTIAL,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ClientApplicationStatus = ClientApplicationStatus.PENDING,
    
    @Column(name = "allowed_scopes", columnDefinition = "TEXT", nullable = false)
    val allowedScopes: String, // JSON array of allowed scopes
    
    @Column(name = "redirect_uris", columnDefinition = "TEXT", nullable = false)
    val redirectUris: String, // JSON array of redirect URIs
    
    @Column(name = "post_logout_redirect_uris", columnDefinition = "TEXT")
    val postLogoutRedirectUris: String? = null, // JSON array of post-logout redirect URIs
    
    @Column(name = "is_trusted", nullable = false)
    val isTrusted: Boolean = false, // Trusted clients skip consent screen
    
    @Column(name = "rate_limit_per_hour")
    val rateLimitPerHour: Int = 1000, // API rate limit per hour
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @JsonIgnore
    val createdBy: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    @JsonIgnore
    val approvedBy: User? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "approved_at")
    val approvedAt: LocalDateTime? = null
)

enum class ClientType {
    CONFIDENTIAL, // Can securely store client credentials (web apps, server-side apps)
    PUBLIC        // Cannot securely store client credentials (mobile apps, SPAs)
}

enum class ClientApplicationStatus {
    PENDING,    // Application submitted, awaiting review
    APPROVED,   // Application approved and active
    SUSPENDED,  // Application temporarily suspended
    REJECTED    // Application rejected
}