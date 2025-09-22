package com.athletetracker.config

import org.springframework.context.annotation.Configuration

/**
 * Security configuration has been moved to ResourceServerConfig to support OAuth2.
 * This class is kept for backwards compatibility but the actual security configuration
 * is now handled by AuthorizationServerConfig and ResourceServerConfig.
 */
@Configuration
class SecurityConfig