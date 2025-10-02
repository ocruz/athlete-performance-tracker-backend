package com.athletetracker.service

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import com.github.mustachejava.MustacheFactory
import com.github.mustachejava.MustacheResolver
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.time.Year
import java.util.concurrent.ConcurrentHashMap

@Service
class MustacheTemplateService {
    
    private val logger = LoggerFactory.getLogger(MustacheTemplateService::class.java)
    private val mustacheFactory: MustacheFactory = DefaultMustacheFactory(CustomMustacheResolver())
    private val templateCache = ConcurrentHashMap<String, Mustache>()
    
    /**
     * Custom resolver to handle email template paths correctly
     */
    inner class CustomMustacheResolver : MustacheResolver {
        override fun getReader(resourceName: String): Reader? {
            return try {
                // Handle different path formats
                val templatePath = when {
                    resourceName.startsWith("templates/email/") -> resourceName
                    resourceName.startsWith("common/") -> "templates/email/$resourceName"
                    else -> "templates/email/$resourceName"
                }
                
                val resource = ClassPathResource("$templatePath.mustache")
                if (resource.exists()) {
                    logger.debug("Loading template: $templatePath")
                    InputStreamReader(resource.inputStream, Charsets.UTF_8)
                } else {
                    logger.warn("Template not found: $templatePath (from resource: $resourceName)")
                    null
                }
            } catch (e: Exception) {
                logger.error("Failed to load template: $resourceName", e)
                null
            }
        }
    }
    
    /**
     * Render a Mustache template with the provided data
     */
    fun renderTemplate(templateName: String, data: Map<String, Any>): String {
        return try {
            val mustache = getOrLoadTemplate(templateName)
            val writer = StringWriter()
            
            // Add common variables to all templates
            val enhancedData = data.toMutableMap().apply {
                put("currentYear", Year.now().value)
            }
            
            mustache.execute(writer, enhancedData)
            writer.toString()
        } catch (e: Exception) {
            logger.error("Failed to render template: $templateName", e)
            throw RuntimeException("Template rendering failed for: $templateName", e)
        }
    }
    
    /**
     * Get template from cache or load it from resources
     */
    private fun getOrLoadTemplate(templateName: String): Mustache {
        return templateCache.computeIfAbsent(templateName) { name ->
            loadTemplate(name)
        }
    }
    
    /**
     * Load template from resources
     */
    private fun loadTemplate(templateName: String): Mustache {
        return try {
            // Use the factory's compile method which will use our custom resolver
            val mustache = mustacheFactory.compile(templateName)
            logger.debug("Loaded template: $templateName")
            mustache
        } catch (e: Exception) {
            logger.error("Failed to load template: $templateName", e)
            throw RuntimeException("Failed to load template: $templateName", e)
        }
    }
    
    /**
     * Clear template cache (useful for development)
     */
    fun clearCache() {
        templateCache.clear()
        logger.info("Template cache cleared")
    }
    
    /**
     * Check if template exists
     */
    fun templateExists(templateName: String): Boolean {
        return try {
            // Try to compile the template - if it exists, this will succeed
            mustacheFactory.compile(templateName)
            true
        } catch (e: Exception) {
            logger.debug("Template not found: $templateName")
            false
        }
    }
    
    /**
     * Preload commonly used templates
     */
    fun preloadTemplates() {
        val commonTemplates = listOf(
            "athlete-invitation",
            "invitation-reminder"
        )
        
        commonTemplates.forEach { templateName ->
            try {
                getOrLoadTemplate(templateName)
                logger.debug("Preloaded template: $templateName")
            } catch (e: Exception) {
                logger.warn("Failed to preload template: $templateName", e)
            }
        }
    }
}