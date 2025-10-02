package com.athletetracker.dto

data class ResendInvitationResponse(
    val success: Boolean,
    val message: String,
    val isReminder: Boolean = false // true if resent existing invitation, false if created new one
)