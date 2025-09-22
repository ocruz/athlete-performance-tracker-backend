package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.Sport
import com.athletetracker.service.AthleteService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/athletes")
@CrossOrigin(origins = ["http://localhost:3000"])
@PreAuthorize("hasRole('COACH') or hasRole('ADMIN')")
class AthletesController(
    private val athleteService: AthleteService
) {

    @GetMapping("/{id}")
    fun getAthleteById(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<AthleteDto> {
        val athlete = athleteService.getAthleteById(id)
        return ResponseEntity.ok(athlete)
    }

    @PostMapping
    fun createAthlete(
        @RequestBody request: CreateAthleteRequest,
        authentication: Authentication
    ): ResponseEntity<AthleteDto> {
        val createdAthlete = athleteService.createAthlete(request)
        return ResponseEntity.ok(createdAthlete)
    }

    @GetMapping
    fun getAllAthletes(authentication: Authentication): ResponseEntity<List<AthleteDto>> {
        val athletes = athleteService.getAllAthletes()
        return ResponseEntity.ok(athletes)
    }

    @GetMapping("/sports/{sport}")
    fun getAthletesBySport(
        @PathVariable sport: Sport,
        authentication: Authentication
    ): ResponseEntity<List<AthleteDto>> {
        val athletes = athleteService.getAthletesBySport(sport)
        return ResponseEntity.ok(athletes)
    }

    @PostMapping("/search")
    fun searchAthletes(
        @RequestBody searchRequest: AthleteSearchRequest,
        authentication: Authentication
    ): ResponseEntity<List<AthleteDto>> {
        val athletes = athleteService.searchAthletes(searchRequest)
        return ResponseEntity.ok(athletes)
    }

    @PutMapping("/{id}")
    fun updateAthlete(
        @PathVariable id: Long,
        @RequestBody request: UpdateAthleteRequest,
        authentication: Authentication
    ): ResponseEntity<AthleteDto> {
        val updatedAthlete = athleteService.updateAthlete(id, request)
        return ResponseEntity.ok(updatedAthlete)
    }

    @DeleteMapping("/{id}")
    fun deleteAthlete(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        athleteService.deleteAthlete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/stats")
    fun getAthleteStats(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        val stats = athleteService.getAthleteStats()
        return ResponseEntity.ok(stats)
    }
}