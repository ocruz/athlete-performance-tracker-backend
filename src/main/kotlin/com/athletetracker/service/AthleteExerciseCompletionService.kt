package com.athletetracker.service

import com.athletetracker.dto.*
import com.athletetracker.entity.*
import com.athletetracker.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class AthleteExerciseCompletionService(
    private val programProgressRepository: ProgramProgressRepository,
    private val athleteProgramRepository: AthleteProgramRepository,
    private val programWorkoutExerciseRepository: ProgramWorkoutExerciseRepository,
    private val userRepository: UserRepository
) {

    fun logProgress(request: LogProgressRequest, loggedById: Long): ProgramProgressResponse {
        val athleteProgram = athleteProgramRepository.findById(request.athleteProgramId)
            .orElseThrow { IllegalArgumentException("Athlete program not found with id: ${request.athleteProgramId}") }
        
        val programWorkoutExercise = programWorkoutExerciseRepository.findById(request.programWorkoutExerciseId)
            .orElseThrow { IllegalArgumentException("Program workout exercise not found with id: ${request.programWorkoutExerciseId}") }
        
        val loggedBy = userRepository.findById(loggedById)
            .orElseThrow { IllegalArgumentException("User not found with id: $loggedById") }

        val progress = AthleteExerciseCompletions(
            athleteProgram = athleteProgram,
            programWorkoutExercise = programWorkoutExercise,
            completedDate = request.completedDate,
            actualSets = request.actualSets,
            actualReps = request.actualReps,
            actualWeight = request.actualWeight,
            actualIntensity = request.actualIntensity,
            restTime = request.restTime,
            completionStatus = request.completionStatus,
            athleteNotes = request.athleteNotes,
            coachNotes = request.coachNotes,
            loggedBy = loggedBy
        )

        val savedProgress = programProgressRepository.save(progress)
        return convertToProgressResponse(savedProgress)
    }

    fun getProgressForAthleteProgram(athleteProgramId: Long): List<ProgramProgressResponse> {
        val progressEntries = programProgressRepository.findByAthleteProgramId(athleteProgramId)
        return progressEntries.map { convertToProgressResponse(it) }
    }

    fun getWeekProgress(athleteProgramId: Long, weekNumber: Int): WeekProgressResponse {
        // TODO: Reimplement with new ProgramWorkout structure
        throw NotImplementedError("Week progress tracking needs to be reimplemented with the new program structure")
    }

    fun getProgressForWeekRange(
        athleteProgramId: Long, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<ProgramProgressResponse> {
        val progressEntries = programProgressRepository.findByAthleteProgramIdAndCompletedDateBetween(
            athleteProgramId, startDate, endDate
        )
        return progressEntries.map { convertToProgressResponse(it) }
    }

    fun updateProgress(progressId: Long, request: LogProgressRequest): ProgramProgressResponse {
        val existingProgress = programProgressRepository.findById(progressId)
            .orElseThrow { IllegalArgumentException("Progress entry not found with id: $progressId") }

        val updatedProgress = existingProgress.copy(
            completedDate = request.completedDate,
            actualSets = request.actualSets,
            actualReps = request.actualReps,
            actualWeight = request.actualWeight,
            actualIntensity = request.actualIntensity,
            restTime = request.restTime,
            completionStatus = request.completionStatus,
            athleteNotes = request.athleteNotes,
            coachNotes = request.coachNotes
        )

        val savedProgress = programProgressRepository.save(updatedProgress)
        return convertToProgressResponse(savedProgress)
    }

    private fun convertToProgressResponse(progress: AthleteExerciseCompletions): ProgramProgressResponse {
        return ProgramProgressResponse(
            id = progress.id,
            athleteProgram = AthleteProgramBasicDto(
                id = progress.athleteProgram.id,
                athleteName = "${progress.athleteProgram.athlete.firstName} ${progress.athleteProgram.athlete.lastName}",
                programName = progress.athleteProgram.program.name,
                startDate = progress.athleteProgram.startDate,
                status = progress.athleteProgram.status.name
            ),
            exercise = ExerciseBasicDto(
                id = progress.programWorkoutExercise.exercise.id,
                name = progress.programWorkoutExercise.exercise.name,
                category = progress.programWorkoutExercise.exercise.category.name,
                muscleGroups = progress.programWorkoutExercise.exercise.muscleGroup.name
            ),
            weekNumber = 0, // TODO: Implement week tracking with new structure
            dayNumber = 0, // TODO: Implement day tracking with new structure
            orderInDay = progress.programWorkoutExercise.orderInWorkout,
            completedDate = progress.completedDate,
            plannedSets = progress.programWorkoutExercise.sets,
            plannedReps = progress.programWorkoutExercise.reps,
            plannedIntensity = progress.programWorkoutExercise.intensityPercentage,
            actualSets = progress.actualSets,
            actualReps = progress.actualReps,
            actualWeight = progress.actualWeight,
            actualIntensity = progress.actualIntensity,
            restTime = progress.restTime,
            completionStatus = progress.completionStatus,
            athleteNotes = progress.athleteNotes,
            coachNotes = progress.coachNotes,
            createdAt = progress.createdAt,
            loggedBy = UserBasicDto(
                id = progress.loggedBy.id,
                firstName = progress.loggedBy.firstName,
                lastName = progress.loggedBy.lastName,
                email = progress.loggedBy.email
            )
        )
    }
}