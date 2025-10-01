package com.athletetracker.repository

import com.athletetracker.entity.Athlete
import com.athletetracker.entity.User
import com.athletetracker.entity.AthleteWorkout
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface AthleteWorkoutRepository : JpaRepository<AthleteWorkout, Long> {
    fun findByAthleteOrderByWorkoutDateDesc(athlete: Athlete): List<AthleteWorkout>
    fun findByCoachOrderByWorkoutDateDesc(coach: User): List<AthleteWorkout>
    
    @Query(
        "SELECT w FROM AthleteWorkout w WHERE w.athlete = :athlete AND " +
           "w.workoutDate BETWEEN :startDate AND :endDate ORDER BY w.workoutDate DESC")
    fun findByAthleteAndDateRange(
        @Param("athlete") athlete: Athlete,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<AthleteWorkout>
    
    fun countByAthlete(athlete: Athlete): Long
    fun countByCoach(coach: User): Long
    
    @Query(
        "SELECT w FROM AthleteWorkout w WHERE w.athlete.id = :athleteId AND " +
           "w.workoutDate BETWEEN :startDate AND :endDate ORDER BY w.workoutDate DESC")
    fun findByAthleteIdAndWorkoutDateBetween(
        @Param("athleteId") athleteId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<AthleteWorkout>
    
    // Find workout by athlete and specific date
    @Query(
        "SELECT w FROM AthleteWorkout w WHERE w.athlete = :athlete AND " +
           "CAST(w.workoutDate AS date) = CAST(:workoutDate AS date)")
    fun findByAthleteAndWorkoutDate(
        @Param("athlete") athlete: Athlete,
        @Param("workoutDate") workoutDate: LocalDateTime
    ): AthleteWorkout?
    
    // Find workouts linked to program templates
    fun findByAthleteAndProgramWorkoutNotNull(athlete: Athlete): List<AthleteWorkout>
}