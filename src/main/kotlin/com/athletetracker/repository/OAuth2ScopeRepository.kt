package com.athletetracker.repository

import com.athletetracker.entity.OAuth2Scope
import com.athletetracker.entity.ScopeCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OAuth2ScopeRepository : JpaRepository<OAuth2Scope, Long> {
    
    fun findByScopeName(scopeName: String): OAuth2Scope?
    
    fun findByCategory(category: ScopeCategory): List<OAuth2Scope>
    
    fun findByIsDefaultTrue(): List<OAuth2Scope>
    
    fun findByIsSensitiveTrue(): List<OAuth2Scope>
    
    fun findByScopeNameIn(scopeNames: List<String>): List<OAuth2Scope>
    
    @Query("SELECT s FROM OAuth2Scope s WHERE s.scopeName IN :scopeNames ORDER BY s.category, s.scopeName")
    fun findByScopeNamesOrdered(@Param("scopeNames") scopeNames: List<String>): List<OAuth2Scope>
    
    @Query("SELECT s FROM OAuth2Scope s ORDER BY s.category, s.scopeName")
    override fun findAll(): List<OAuth2Scope>
}