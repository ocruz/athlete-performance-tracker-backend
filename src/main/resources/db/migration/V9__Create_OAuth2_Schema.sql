-- OAuth2 Authorization Server Database Schema
-- This migration creates the necessary tables for OAuth2 Authorization Server functionality

-- OAuth2 Registered Client table (Spring Authorization Server)
CREATE TABLE oauth2_registered_client (
    id VARCHAR(100) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret VARCHAR(200) DEFAULT NULL,
    client_secret_expires_at TIMESTAMP DEFAULT NULL,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL,
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL,
    PRIMARY KEY (id)
);

-- OAuth2 Authorization table (Spring Authorization Server)
CREATE TABLE oauth2_authorization (
    id VARCHAR(100) NOT NULL,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorization_grant_type VARCHAR(100) NOT NULL,
    authorized_scopes VARCHAR(1000) DEFAULT NULL,
    attributes TEXT DEFAULT NULL,
    state VARCHAR(500) DEFAULT NULL,
    authorization_code_value TEXT DEFAULT NULL,
    authorization_code_issued_at TIMESTAMP DEFAULT NULL,
    authorization_code_expires_at TIMESTAMP DEFAULT NULL,
    authorization_code_metadata TEXT DEFAULT NULL,
    access_token_value TEXT DEFAULT NULL,
    access_token_issued_at TIMESTAMP DEFAULT NULL,
    access_token_expires_at TIMESTAMP DEFAULT NULL,
    access_token_metadata TEXT DEFAULT NULL,
    access_token_type VARCHAR(100) DEFAULT NULL,
    access_token_scopes VARCHAR(1000) DEFAULT NULL,
    oidc_id_token_value TEXT DEFAULT NULL,
    oidc_id_token_issued_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_expires_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_metadata TEXT DEFAULT NULL,
    refresh_token_value TEXT DEFAULT NULL,
    refresh_token_issued_at TIMESTAMP DEFAULT NULL,
    refresh_token_expires_at TIMESTAMP DEFAULT NULL,
    refresh_token_metadata TEXT DEFAULT NULL,
    user_code_value TEXT DEFAULT NULL,
    user_code_issued_at TIMESTAMP DEFAULT NULL,
    user_code_expires_at TIMESTAMP DEFAULT NULL,
    user_code_metadata TEXT DEFAULT NULL,
    device_code_value TEXT DEFAULT NULL,
    device_code_issued_at TIMESTAMP DEFAULT NULL,
    device_code_expires_at TIMESTAMP DEFAULT NULL,
    device_code_metadata TEXT DEFAULT NULL,
    PRIMARY KEY (id)
);

-- OAuth2 Authorization Consent table (Spring Authorization Server)
CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

-- Custom Client Application Management table
CREATE TABLE client_applications (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(100) UNIQUE NOT NULL,
    client_name VARCHAR(200) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    website_url VARCHAR(500),
    privacy_policy_url VARCHAR(500),
    terms_of_service_url VARCHAR(500),
    contact_email VARCHAR(255),
    client_type VARCHAR(20) NOT NULL DEFAULT 'CONFIDENTIAL', -- CONFIDENTIAL, PUBLIC
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, SUSPENDED, REJECTED
    allowed_scopes TEXT NOT NULL, -- JSON array of allowed scopes
    redirect_uris TEXT NOT NULL, -- JSON array of redirect URIs
    post_logout_redirect_uris TEXT, -- JSON array of post-logout redirect URIs
    is_trusted BOOLEAN DEFAULT FALSE, -- Trusted clients skip consent screen
    rate_limit_per_hour INTEGER DEFAULT 1000, -- API rate limit per hour
    created_by_id BIGINT NOT NULL,
    approved_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    approved_at TIMESTAMP,
    FOREIGN KEY (created_by_id) REFERENCES users(id),
    FOREIGN KEY (approved_by_id) REFERENCES users(id)
);

-- OAuth2 Scope Definitions table
CREATE TABLE oauth2_scopes (
    id BIGSERIAL PRIMARY KEY,
    scope_name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL, -- ATHLETE_DATA, PERFORMANCE, WORKOUTS, ASSESSMENTS, PROGRAMS
    is_sensitive BOOLEAN DEFAULT FALSE, -- Requires additional consent
    is_default BOOLEAN DEFAULT FALSE, -- Included by default in client registrations
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- OAuth2 Access Logs table for monitoring and analytics
CREATE TABLE oauth2_access_logs (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    user_id BIGINT,
    scope VARCHAR(100) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    response_status INTEGER,
    response_time_ms INTEGER,
    accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes for performance
CREATE INDEX idx_oauth2_registered_client_client_id ON oauth2_registered_client(client_id);
CREATE INDEX idx_oauth2_authorization_registered_client_id ON oauth2_authorization(registered_client_id);
CREATE INDEX idx_oauth2_authorization_principal_name ON oauth2_authorization(principal_name);
CREATE INDEX idx_oauth2_authorization_consent_registered_client_id ON oauth2_authorization_consent(registered_client_id);
CREATE INDEX idx_oauth2_authorization_consent_principal_name ON oauth2_authorization_consent(principal_name);

CREATE INDEX idx_client_applications_client_id ON client_applications(client_id);
CREATE INDEX idx_client_applications_status ON client_applications(status);
CREATE INDEX idx_client_applications_created_by ON client_applications(created_by_id);

CREATE INDEX idx_oauth2_scopes_scope_name ON oauth2_scopes(scope_name);
CREATE INDEX idx_oauth2_scopes_category ON oauth2_scopes(category);

CREATE INDEX idx_oauth2_access_logs_client_id ON oauth2_access_logs(client_id);
CREATE INDEX idx_oauth2_access_logs_user_id ON oauth2_access_logs(user_id);
CREATE INDEX idx_oauth2_access_logs_accessed_at ON oauth2_access_logs(accessed_at);

-- Insert default OAuth2 scopes
INSERT INTO oauth2_scopes (scope_name, display_name, description, category, is_sensitive, is_default) VALUES
('athlete:read', 'Read Athlete Profile', 'Access to basic athlete profile information (name, sport, position)', 'ATHLETE_DATA', false, true),
('athlete:contact', 'Read Contact Information', 'Access to athlete contact information (email, phone)', 'ATHLETE_DATA', true, false),
('performance:read', 'Read Performance Metrics', 'Access to performance metrics and personal records', 'PERFORMANCE', false, true),
('workouts:read', 'Read Workout Data', 'Access to workout history and exercise data', 'WORKOUTS', false, true),
('assessments:read', 'Read Assessment Results', 'Access to fitness assessment results and progress', 'ASSESSMENTS', false, true),
('programs:read', 'Read Training Programs', 'Access to assigned training programs and schedules', 'PROGRAMS', false, false),
('progress:read', 'Read Progress Data', 'Access to progress tracking and trend analysis', 'PERFORMANCE', false, false),
('medical:read', 'Read Medical Information', 'Access to medical notes and injury history', 'ATHLETE_DATA', true, false);

-- Insert a default trusted client for testing
INSERT INTO oauth2_registered_client (
    id, 
    client_id, 
    client_name, 
    client_authentication_methods, 
    authorization_grant_types, 
    redirect_uris, 
    scopes, 
    client_settings, 
    token_settings
) VALUES (
    'default-client-id',
    'athlete-tracker-mobile',
    'Athlete Tracker Mobile App',
    'client_secret_basic',
    'authorization_code,refresh_token',
    'https://localhost:3000/callback,com.athletetracker.mobile://callback',
    'athlete:read,performance:read,workouts:read,assessments:read',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":true,"settings.client.require-authorization-consent":true}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":true,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",3600.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}'
);