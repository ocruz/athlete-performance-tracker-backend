package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_profiles")
data class UserProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @JsonBackReference
    val user: User,

    // Universal fields for all user types
    @Column(name = "phone")
    val phone: String? = null,

    @Column(name = "profile_photo_url")
    val profilePhotoUrl: String? = null,

    @Column(name = "notification_preferences", length = 1000)
    val notificationPreferences: String? = null,

    @Column(name = "preferred_language")
    val preferredLanguage: String? = null,

    @Column(name = "timezone")
    val timezone: String? = null,

    @Column(name = "profile_visibility")
    val profileVisibility: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)