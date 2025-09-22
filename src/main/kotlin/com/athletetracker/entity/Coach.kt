package com.athletetracker.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "coaches")
data class Coach(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val firstName: String,
    
    @Column(nullable = false)
    val lastName: String,
    
    @Column
    val email: String? = null,
    
    @Column
    val phone: String? = null,
    
    @Column
    val officeLocation: String? = null,
    
    @Column
    val yearsExperience: Int? = null,
    
    @Column
    val certifications: String? = null,
    
    @Column
    val specializations: String? = null,
    
    @Column
    val coachingPhilosophy: String? = null,
    
    @Column
    val preferredSports: String? = null,
    
    @Column
    val preferredContactMethod: String? = null,
    
    @Column
    val availabilityHours: String? = null,
    
    @Column
    val bio: String? = null,
    
    @Column
    val profilePhotoUrl: String? = null,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(unique = true)
    val userId: Long? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
}