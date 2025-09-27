package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
data class User @JsonCreator constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    @JsonProperty("email")
    val email: String,
    
    @Column(nullable = false)
    @JsonProperty("password")
    private val password: String?,
    
    @Column(nullable = false)
    @JsonProperty("firstName")
    val firstName: String,
    
    @Column(nullable = false)
    @JsonProperty("lastName")
    val lastName: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty("role")
    val role: UserRole = UserRole.COACH,
    
    @Column(nullable = false)
    @JsonProperty("isActive")
    @JsonAlias("active")
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    @JsonProperty("createdAt")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    @JsonProperty("updatedAt")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    @JsonProperty("lastLoginAt")
    val lastLoginAt: LocalDateTime? = null,
    
    @Column
    @JsonProperty("defaultRoute")
    val defaultRoute: String? = null // role-based default landing page
) : UserDetails {
    
    @JsonIgnore
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    }
    
    @JsonIgnore
    override fun getPassword(): String? = password
    
    @JsonIgnore
    override fun getUsername(): String = email
    
    @JsonIgnore
    override fun isAccountNonExpired(): Boolean = true
    
    @JsonIgnore
    override fun isAccountNonLocked(): Boolean = true
    
    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean = true
    
    @JsonIgnore
    override fun isEnabled(): Boolean = isActive

    val fullName: String
        @JsonIgnore
        get() = "$firstName $lastName"
}

enum class UserRole {
    ADMIN,
    COACH,
    ATHLETE
}