package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.Workout
import com.athletetracker.service.AthleteProgramService
import com.athletetracker.service.AutoScheduleProgramRequest
import com.athletetracker.service.ProgramProgressService
import com.athletetracker.service.ScheduleWorkoutForDayRequest
import com.athletetracker.service.ScheduleWorkoutsForWeekRequest
import com.athletetracker.service.WorkoutGenerationService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/athlete-programs")
@CrossOrigin(origins = ["http://localhost:3000"])
@PreAuthorize("hasRole('COACH') or hasRole('ATHLETE')")
class AthleteProgramController(
    private val athleteProgramService: AthleteProgramService,
    private val programProgressService: ProgramProgressService,
    private val workoutGenerationService: WorkoutGenerationService
) {

    @PostMapping("/assign")
    @PreAuthorize("hasRole('COACH')")
    fun assignProgram(
        @RequestBody request: AssignProgramRequest,
        @RequestParam assignedById: Long
    ): ResponseEntity<AthleteProgramResponse> {
        val response = athleteProgramService.assignProgram(request, assignedById)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/athlete/{athleteId}")
    fun getAthletePrograms(@PathVariable athleteId: Long): ResponseEntity<List<AthleteProgramResponse>> {
        val programs = athleteProgramService.getAthletePrograms(athleteId)
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/athlete/{athleteId}/active")
    fun getActivePrograms(@PathVariable athleteId: Long): ResponseEntity<List<AthleteProgramResponse>> {
        val activePrograms = athleteProgramService.getActiveProgramsForAthlete(athleteId)
        return ResponseEntity.ok(activePrograms)
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasRole('COACH')")
    fun getProgramsByCoach(@PathVariable coachId: Long): ResponseEntity<List<AthleteProgramResponse>> {
        val programs = athleteProgramService.getProgramsByCoach(coachId)
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/{athleteProgramId}")
    fun getAthleteProgramDetail(@PathVariable athleteProgramId: Long): ResponseEntity<AthleteProgramResponse> {
        val program = athleteProgramService.getAthleteProgramDetail(athleteProgramId)
        return ResponseEntity.ok(program)
    }

    @PutMapping("/{athleteProgramId}/status")
    fun updateProgramStatus(
        @PathVariable athleteProgramId: Long,
        @RequestBody request: UpdateProgramStatusRequest
    ): ResponseEntity<AthleteProgramResponse> {
        val response = athleteProgramService.updateProgramStatus(athleteProgramId, request)
        return ResponseEntity.ok(response)
    }

    // Progress endpoints
    @PostMapping("/progress")
    fun logProgress(
        @RequestBody request: LogProgressRequest,
        @RequestParam loggedById: Long
    ): ResponseEntity<ProgramProgressResponse> {
        val response = programProgressService.logProgress(request, loggedById)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{athleteProgramId}/progress")
    fun getProgressForAthleteProgram(@PathVariable athleteProgramId: Long): ResponseEntity<List<ProgramProgressResponse>> {
        val progress = programProgressService.getProgressForAthleteProgram(athleteProgramId)
        return ResponseEntity.ok(progress)
    }

    @GetMapping("/{athleteProgramId}/progress/week/{weekNumber}")
    fun getWeekProgress(
        @PathVariable athleteProgramId: Long,
        @PathVariable weekNumber: Int
    ): ResponseEntity<WeekProgressResponse> {
        val weekProgress = programProgressService.getWeekProgress(athleteProgramId, weekNumber)
        return ResponseEntity.ok(weekProgress)
    }

    @GetMapping("/{athleteProgramId}/progress/range")
    fun getProgressForDateRange(
        @PathVariable athleteProgramId: Long,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate
    ): ResponseEntity<List<ProgramProgressResponse>> {
        val progress = programProgressService.getProgressForWeekRange(athleteProgramId, startDate, endDate)
        return ResponseEntity.ok(progress)
    }

    @PutMapping("/progress/{progressId}")
    fun updateProgress(
        @PathVariable progressId: Long,
        @RequestBody request: LogProgressRequest
    ): ResponseEntity<ProgramProgressResponse> {
        val response = programProgressService.updateProgress(progressId, request)
        return ResponseEntity.ok(response)
    }

    // Workout Generation Endpoints
    @PostMapping("/{athleteProgramId}/generate-workouts")
    fun generateWorkouts(
        @PathVariable athleteProgramId: Long,
        @RequestBody request: GenerateWorkoutsRequest
    ): ResponseEntity<List<WorkoutDto>> {
        val startDate = LocalDate.parse(request.startDate)
        val workouts = workoutGenerationService.generateWorkoutsFromProgram(
            athleteProgramId = athleteProgramId,
            startDate = startDate,
            numberOfWeeks = request.numberOfWeeks
        )
        return ResponseEntity.ok(workouts)
    }

    @GetMapping("/{athleteProgramId}/workouts")
    fun getGeneratedWorkouts(@PathVariable athleteProgramId: Long): ResponseEntity<List<WorkoutDto>> {
        val workouts = workoutGenerationService.getGeneratedWorkouts(athleteProgramId)
        return ResponseEntity.ok(workouts)
    }

    @PostMapping("/workouts/{workoutId}/regenerate")
    fun regenerateWorkout(@PathVariable workoutId: Long): ResponseEntity<WorkoutDto> {
        val workout = workoutGenerationService.regenerateWorkoutFromTemplate(workoutId)
        return ResponseEntity.ok(workout)
    }
}