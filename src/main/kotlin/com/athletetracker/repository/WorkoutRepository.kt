package com.athletetracker.repository

import com.athletetracker.entity.Athlete
import com.athletetracker.entity.User
import com.athletetracker.entity.Workout
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface WorkoutRepository : JpaRepository<Workout, Long> {
    fun findByAthleteOrderByWorkoutDateDesc(athlete: Athlete): List<Workout>
    fun findByCoachOrderByWorkoutDateDesc(coach: User): List<Workout>
    
    @Query("SELECT w FROM Workout w WHERE w.athlete = :athlete AND " +
           "w.workoutDate BETWEEN :startDate AND :endDate ORDER BY w.workoutDate DESC")
    fun findByAthleteAndDateRange(
        @Param("athlete") athlete: Athlete,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Workout>
    
    fun countByAthlete(athlete: Athlete): Long
    fun countByCoach(coach: User): Long
    
    @Query("SELECT w FROM Workout w WHERE w.athlete.id = :athleteId AND " +
           "w.workoutDate BETWEEN :startDate AND :endDate ORDER BY w.workoutDate DESC")
    fun findByAthleteIdAndWorkoutDateBetween(
        @Param("athleteId") athleteId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Workout>
    
    // Find workout by athlete and specific date
    @Query("SELECT w FROM Workout w WHERE w.athlete = :athlete AND " +
           "CAST(w.workoutDate AS date) = CAST(:workoutDate AS date)")
    fun findByAthleteAndWorkoutDate(
        @Param("athlete") athlete: Athlete,
        @Param("workoutDate") workoutDate: LocalDateTime
    ): Workout?
    
    // Find workouts linked to program templates
    fun findByAthleteAndProgramWorkoutNotNull(athlete: Athlete): List<Workout>
}