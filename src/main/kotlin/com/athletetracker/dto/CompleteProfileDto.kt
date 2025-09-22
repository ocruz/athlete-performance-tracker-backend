package com.athletetracker.dto

import com.athletetracker.entity.UserRole

sealed class CompleteProfileDto {
    data class CoachProfile(
        val user: UserDto,
        val coach: CoachDto?,
        val preferences: UserProfileDto?
    ) : CompleteProfileDto()
    
    data class AthleteProfile(
        val user: UserDto,
        val athlete: AthleteDto?,
        val preferences: UserProfileDto?
    ) : CompleteProfileDto()
    
    data class BasicProfile(
        val user: UserDto,
        val preferences: UserProfileDto?
    ) : CompleteProfileDto()
    
    companion object {
        fun forCoach(user: UserDto, coach: CoachDto?, preferences: UserProfileDto?): CoachProfile {
            return CoachProfile(user, coach, preferences)
        }
        
        fun forAthlete(user: UserDto, athlete: AthleteDto?, preferences: UserProfileDto?): AthleteProfile {
            return AthleteProfile(user, athlete, preferences)
        }
        
        fun forUser(user: UserDto, preferences: UserProfileDto?): BasicProfile {
            return BasicProfile(user, preferences)
        }
    }
}

