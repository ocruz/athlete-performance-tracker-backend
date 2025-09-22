package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.Athlete
import com.athletetracker.entity.CompletionStatus
import com.athletetracker.entity.ProgramStatus
import com.athletetracker.entity.User
import com.athletetracker.entity.UserRole
import com.athletetracker.repository.*
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.ceil

@Service
class AthleteDashboardService(
    private val athleteRepository: AthleteRepository,
    private val athleteProgramRepository: AthleteProgramRepository,
    private val programProgressRepository: ProgramProgressRepository
) {

    fun getDashboardData(user: User): AthleteDashboardResponse {
        // For now, if the user is not an athlete, we'll still provide a basic response
        // In a real system; you might want to find the athlete record associated with this user
        
        return when (user.role) {
            UserRole.ATHLETE -> {
                // Find athlete record associated with this user
                val athlete = findAthleteByUser(user)
                if (athlete != null) {
                    buildDashboardForAthlete(athlete.id)
                } else {
                    buildEmptyDashboard(user)
                }
            }
            else -> {
                // For coaches/admins viewing athlete dashboard, provide basic view
                buildEmptyDashboard(user)
            }
        }
    }

    private fun findAthleteByUser(user: User): Athlete? {
        // This assumes athletes have matching email addresses
        // In a real system, you might have a direct relationship
        return athleteRepository.findByEmail(user.email)
    }

    private fun buildDashboardForAthlete(athleteId: Long): AthleteDashboardResponse {
        val athlete = athleteRepository.findById(athleteId).orElseThrow()
        
        // Get active programs
        val activePrograms = athleteProgramRepository.findByAthleteIdAndStatus(
            athleteId, 
            ProgramStatus.ACTIVE
        )
        
        val programSummaries = activePrograms.map { athleteProgram ->
            val progressEntries = programProgressRepository.findByAthleteProgramId(athleteProgram.id)
            val totalExercises = athleteProgram.program.programWorkouts.size
            val completedExercises = progressEntries.count { it.completionStatus == CompletionStatus.COMPLETED }
            
            val completionPercentage = if (totalExercises > 0) {
                (completedExercises.toDouble() / totalExercises) * 100
            } else 0.0
            
            // Calculate current week based on start date
            val startDate = athleteProgram.startDate
            val currentDate = LocalDate.now()
            val weeksElapsed = ceil(
                ChronoUnit.DAYS.between(startDate, currentDate) / 7.0
            ).toInt()
            
            // Calculate adherence rate (simplified)
            val expectedWorkouts = weeksElapsed * 3 // Assuming 3 workouts per week
            val completedWorkouts = progressEntries.size
            val adherenceRate = if (expectedWorkouts > 0) {
                (completedWorkouts.toDouble() / expectedWorkouts) * 100
            } else 100.0
            
            AthleteProgramSummaryDto(
                id = athleteProgram.program.id,
                name = athleteProgram.program.name,
                completionPercentage = completionPercentage,
                currentWeek = weeksElapsed.coerceAtLeast(1),
                adherenceRate = adherenceRate.coerceAtMost(100.0),
                startDate = athleteProgram.startDate,
                endDate = athleteProgram.endDate
            )
        }
        
        // Calculate progress highlights
        val thisWeek = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfYear())
        val thisWeekWorkouts = programProgressRepository.findByAthleteIdAndWeek(athleteId, thisWeek).size
        val totalWorkouts = programProgressRepository.findByAthleteId(athleteId).size
        
        // Mock some metrics for now
        val progressHighlights = ProgressHighlightsDto(
            thisWeekWorkouts = thisWeekWorkouts,
            totalWorkouts = totalWorkouts,
            averageRPE = 7.5, // Mock value
            strengthImprovement = 12.3 // Mock value
        )
        
        return AthleteDashboardResponse(
            athlete = AthleteBasicDto(
                id = athlete.id,
                firstName = athlete.firstName,
                lastName = athlete.lastName,
                sport = athlete.sport.name,
                dateOfBirth = athlete.dateOfBirth
            ),
            activePrograms = programSummaries,
            progressHighlights = progressHighlights,
            upcomingWorkouts = emptyList() // TODO: Implement upcoming workouts
        )
    }

    private fun buildEmptyDashboard(user: User): AthleteDashboardResponse {
        return AthleteDashboardResponse(
            athlete = AthleteBasicDto(
                id = 0,
                firstName = user.firstName,
                lastName = user.lastName,
                dateOfBirth = LocalDate.of(1990, 1, 1), // Default date
                sport = "Unknown"
            ),
            activePrograms = emptyList(),
            progressHighlights = ProgressHighlightsDto(
                thisWeekWorkouts = 0,
                totalWorkouts = 0,
                averageRPE = 0.0,
                strengthImprovement = 0.0
            ),
            upcomingWorkouts = emptyList()
        )
    }
}