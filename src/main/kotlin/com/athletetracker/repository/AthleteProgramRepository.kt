package com.athletetracker.repository

import com.athletetracker.entity.AthleteProgram
import com.athletetracker.entity.ProgramStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AthleteProgramRepository : JpaRepository<AthleteProgram, Long> {
    
    fun findByAthleteIdAndStatus(athleteId: Long, status: ProgramStatus): List<AthleteProgram>
    
    fun findByAthleteId(athleteId: Long): List<AthleteProgram>
    
    fun findByProgramId(programId: Long): List<AthleteProgram>
    
    fun findByAssignedById(assignedById: Long): List<AthleteProgram>
    
    @Query("SELECT ap FROM AthleteProgram ap WHERE ap.athlete.id = :athleteId AND ap.status = 'ACTIVE'")
    fun findActiveByAthleteId(@Param("athleteId") athleteId: Long): List<AthleteProgram>
    
    @Query("SELECT ap FROM AthleteProgram ap WHERE ap.program.createdBy.id = :coachId")
    fun findByCoachId(@Param("coachId") coachId: Long): List<AthleteProgram>
    
    @Query("""
        SELECT COUNT(ap) FROM AthleteProgram ap 
        WHERE ap.athlete.id = :athleteId AND ap.status = :status
    """)
    fun countByAthleteIdAndStatus(@Param("athleteId") athleteId: Long, @Param("status") status: ProgramStatus): Long
    
    @Query("SELECT ap FROM AthleteProgram ap WHERE ap.athlete.id = :athleteId AND ap.program.id = :programId AND ap.status = 'ACTIVE'")
    fun findActiveByAthleteIdAndProgramId(@Param("athleteId") athleteId: Long, @Param("programId") programId: Long): AthleteProgram?
    
    fun countByStatus(status: ProgramStatus): Long
}