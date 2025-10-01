package com.athletetracker.dto

import com.athletetracker.entity.Sport

/**
 * Basic Program DTO for standard CRUD operations
 */
data class ProgramDto(
    val id: Long,
    val name: String,
    val description: String?,
    val sport: Sport,
    val durationWeeks: Int?,
    val difficultyLevel: String?,
    val goals: String?,
    val isActive: Boolean,
    val isTemplate: Boolean,
    val createdBy: UserBasicDto,
    val createdAt: String,
    val updatedAt: String
)