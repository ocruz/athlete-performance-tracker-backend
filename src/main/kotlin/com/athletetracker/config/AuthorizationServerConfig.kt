package com.athletetracker.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

@Configuration
@EnableWebSecurity
class AuthorizationServerConfig(
    private val frontendLoginRedirectEntryPoint: FrontendLoginRedirectEntryPoint
) {

    @Bean
    @Order(1)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)

        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
            .oidc(Customizer.withDefaults()) // Enable OpenID Connect 1.0

        http
            // Only apply to OAuth2 endpoints, not all endpoints
            .securityMatcher(
                "/oauth2/**",
                "/connect/**",
                "/userinfo",
                "/.well-known/**"
            )
            // Redirect to the frontend login page when not authenticated from the authorization endpoint
            .exceptionHandling { exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    frontendLoginRedirectEntryPoint,
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            }
            // Accept access tokens for User Info and/or Client Registration
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt(Customizer.withDefaults())
            }
            .csrf { it.disable() }

        return http.build()
    }

    @Bean
    fun registeredClientRepository(jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate): RegisteredClientRepository {
        return JdbcRegisteredClientRepository(jdbcTemplate)
    }

    @Bean
    fun authorizationConsentService(
        jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate,
        registeredClientRepository: RegisteredClientRepository
    ): OAuth2AuthorizationConsentService {
        return JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository)
    }

    @Bean
    fun authorizationService(
        jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate,
        registeredClientRepository: RegisteredClientRepository
    ): OAuth2AuthorizationService {
        val rowMapper = JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);
        val oAuth2AuthorizationParametersMapper = JdbcOAuth2AuthorizationService.OAuth2AuthorizationParametersMapper();
        val authorizationService = JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
        val objectMapper = ObjectMapper();

        val classLoader = JdbcOAuth2AuthorizationService::class.java.classLoader
        val securityModules = SecurityJackson2Modules.getModules(classLoader)
        objectMapper.registerModules(securityModules)
        objectMapper.registerModule(KotlinModule.Builder().build())
        objectMapper.registerModule(OAuth2AuthorizationServerJackson2Module())

        // Enable default typing for OAuth2 framework classes
        SecurityJackson2Modules.enableDefaultTyping(objectMapper)

//        objectMapper.activateDefaultTyping(
//            LaissezFaireSubTypeValidator.instance,
//            ObjectMapper.DefaultTyping.NON_FINAL,
//            JsonTypeInfo.As.PROPERTY
//        )

        rowMapper.setObjectMapper(objectMapper);
        oAuth2AuthorizationParametersMapper.setObjectMapper(objectMapper);
        authorizationService.setAuthorizationRowMapper(rowMapper);
        authorizationService.setAuthorizationParametersMapper(oAuth2AuthorizationParametersMapper);

        return authorizationService;
    }

    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val keyPair = generateRsaKey()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val rsaKey = RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    private fun generateRsaKey(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)
    }


    @Bean
    fun authorizationServerSettings(
        @Value("\${app.oauth2.issuer-uri:http://localhost:8081}") issuerUri: String
    ): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder()
            .issuer(issuerUri)
            .authorizationEndpoint("/oauth2/authorize")
            .deviceAuthorizationEndpoint("/oauth2/device_authorization")
            .deviceVerificationEndpoint("/oauth2/device_verification")
            .tokenEndpoint("/oauth2/token")
            .tokenIntrospectionEndpoint("/oauth2/introspect")
            .tokenRevocationEndpoint("/oauth2/revoke")
            .jwkSetEndpoint("/oauth2/jwks")
            .oidcLogoutEndpoint("/connect/logout")
            .oidcUserInfoEndpoint("/userinfo")
            .oidcClientRegistrationEndpoint("/connect/register")
            .build()
    }
}

