package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.net.InetAddress
import java.time.LocalDateTime

@Entity
@Table(name = "oauth2_access_logs")
data class OAuth2AccessLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "client_id", nullable = false)
    val clientId: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    val user: User? = null,
    
    @Column(nullable = false)
    val scope: String,
    
    @Column(nullable = false)
    val endpoint: String,
    
    @Column(nullable = false)
    val method: String,
    
    @Column(name = "ip_address")
    val ipAddress: InetAddress? = null,
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    val userAgent: String? = null,
    
    @Column(name = "response_status")
    val responseStatus: Int? = null,
    
    @Column(name = "response_time_ms")
    val responseTimeMs: Int? = null,
    
    @Column(name = "accessed_at", nullable = false)
    val accessedAt: LocalDateTime = LocalDateTime.now()
)