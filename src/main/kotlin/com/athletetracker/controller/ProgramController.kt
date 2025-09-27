package com.athletetracker.controller

import com.athletetracker.dto.*
import com.athletetracker.entity.Program
import com.athletetracker.entity.Sport
import com.athletetracker.service.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/programs")
class ProgramController(
    private val programService: ProgramService
) {

    @PostMapping
    fun createBasicProgram(@RequestBody request: CreateBasicProgramRequest): ResponseEntity<Program> {
        val program = programService.createBasicProgram(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(program)
    }

    @GetMapping("/{id}")
    fun getProgramById(@PathVariable id: Long): ResponseEntity<ProgramDetailResponse> {
        val program = programService.getProgramById(id)
        return ResponseEntity.ok(program)
    }

    @GetMapping
    fun getAllActivePrograms(): ResponseEntity<List<Program>> {
        val programs = programService.getAllActivePrograms()
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/templates")
    fun getProgramTemplates(): ResponseEntity<List<Program>> {
        val templates = programService.getProgramTemplates()
        return ResponseEntity.ok(templates)
    }

    @GetMapping("/sport/{sport}")
    fun getProgramsBySport(@PathVariable sport: Sport): ResponseEntity<List<Program>> {
        val programs = programService.getProgramsBySport(sport)
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/sport/{sport}/templates")
    fun getProgramTemplatesBySport(@PathVariable sport: Sport): ResponseEntity<List<Program>> {
        val templates = programService.getProgramTemplatesBySport(sport)
        return ResponseEntity.ok(templates)
    }

    @GetMapping("/creator/{creatorId}")
    fun getProgramsByCreator(@PathVariable creatorId: Long): ResponseEntity<List<Program>> {
        val programs = programService.getProgramsByCreator(creatorId)
        return ResponseEntity.ok(programs)
    }

    @GetMapping("/search")
    fun searchPrograms(@RequestParam query: String): ResponseEntity<List<Program>> {
        val programs = programService.searchPrograms(query)
        return ResponseEntity.ok(programs)
    }

    @PutMapping("/{id}")
    fun updateProgram(
        @PathVariable id: Long,
        @RequestBody request: UpdateProgramRequest
    ): ResponseEntity<Program> {
        val program = programService.updateProgram(id, request)
        return ResponseEntity.ok(program)
    }

    @DeleteMapping("/{id}")
    fun deleteProgram(@PathVariable id: Long): ResponseEntity<Void> {
        programService.deleteProgram(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/duplicate")
    fun duplicateProgram(
        @PathVariable id: Long,
        @RequestBody request: DuplicateProgramRequest
    ): ResponseEntity<Program> {
        val program = programService.duplicateProgram(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(program)
    }

    @PostMapping("/initialize-defaults")
    fun initializeDefaultPrograms(): ResponseEntity<String> {
        programService.initializeDefaultPrograms()
        return ResponseEntity.ok("Default programs initialized successfully")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }
}