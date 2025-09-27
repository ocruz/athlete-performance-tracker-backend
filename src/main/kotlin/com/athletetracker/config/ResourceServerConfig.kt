package com.athletetracker.config

import com.athletetracker.security.CustomUserDetailsService
import com.athletetracker.security.JwtAuthenticationEntryPoint
import com.athletetracker.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class ResourceServerConfig(
    private val corsConfigurationSource: CorsConfigurationSource,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val userDetailsService: CustomUserDetailsService
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }


    @Bean
    @Order(2)
    fun oauth2LoginSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/auth/oauth2/**")
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/auth/oauth2/**").permitAll()
            }
            .authenticationProvider(authenticationProvider())

        return http.build()
    }

    @Bean
    @Order(3)
    fun oauth2ResourceSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/v1/oauth2/**")
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // OAuth2 API endpoints (protected by scopes)
                    .requestMatchers("/v1/oauth2/**").hasAuthority("SCOPE_openid")
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        return http.build()
    }

    @Bean
    @Order(4)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    
                    // OAuth2 endpoints
                    .requestMatchers("/oauth2/**").permitAll()
                    .requestMatchers("/connect/**").permitAll()
                    .requestMatchers("/userinfo").permitAll()
                    .requestMatchers("/.well-known/**").permitAll()
                    .requestMatchers("/login").permitAll()
                    
                    // API documentation
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    
                    // Role-based endpoints (existing JWT authentication)
                    .requestMatchers("/api/athlete/**").hasRole("ATHLETE")
                    .requestMatchers("/api/coach/**").hasRole("COACH")
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    
                    // General API endpoints (accessible by authenticated users)
                    .requestMatchers(HttpMethod.GET, "/api/exercises/**").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/programs/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/programs").hasAnyRole("COACH", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/programs/**").hasAnyRole("COACH", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/programs/**").hasAnyRole("COACH", "ADMIN")
                    
                    // All other requests require authentication
                    .anyRequest().authenticated()
            }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        // H2 Console specific settings
        http.headers { headers ->
            headers.frameOptions { it.sameOrigin() }
        }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        
        // Convert scopes to authorities with SCOPE_ prefix
        grantedAuthoritiesConverter.setAuthorityPrefix("SCOPE_")
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope")

        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
        
        return jwtAuthenticationConverter
    }
}