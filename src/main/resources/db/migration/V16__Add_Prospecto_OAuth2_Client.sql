-- Add the Prospecto Performance OAuth2 client to fix 404 authorization errors
-- This client matches the failing request: client_id=prospecto

-- First, generate a client secret (we'll use a placeholder for now and update with proper encryption)
-- For development, we'll use a simple secret that can be updated later

INSERT INTO oauth2_registered_client (
    id,
    client_id,
    client_id_issued_at,
    client_secret,
    client_secret_expires_at,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    post_logout_redirect_uris,
    scopes,
    client_settings,
    token_settings
) VALUES (
    'prospecto-client-uuid',
    'prospecto',
    NOW(),
    '{bcrypt}$2a$12$6VJkQ8ZZv4NfYKcJ.FV9GewYIiw3n/YrXKWJVF0lGWnLSKp.E5Hyi', -- BCrypt hash of "prospecto-secret"
    NULL, -- No expiration
    'Prospecto Performance Application',
    'client_secret_basic',
    'authorization_code,refresh_token',
    'http://localhost:8080/api/oauth2/athlete-tracker/callback,http://localhost:3001/auth/callback,https://localhost:3001/auth/callback',
    'http://localhost:8080,http://localhost:3001,https://localhost:3001',
    'athlete:read,performance:read,workouts:read,assessments:read',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":true}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",3600.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}'
);

-- Also add a development/testing client with simpler configuration
INSERT INTO oauth2_registered_client (
    id,
    client_id,
    client_id_issued_at,
    client_secret,
    client_secret_expires_at,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    post_logout_redirect_uris,
    scopes,
    client_settings,
    token_settings
) VALUES (
    'dev-client-uuid',
    'prospecto-dev',
    NOW(),
    '{bcrypt}$2a$12$K9rT5L.7Q3nZc8VkF2N4U.w1QJP9Y7HtGvXcR6BmW8pN.A4D2E9Fy', -- BCrypt hash of "dev-secret"
    NULL,
    'Prospecto Development Client',
    'client_secret_basic',
    'authorization_code,refresh_token,client_credentials',
    'http://localhost:8080/api/oauth2/athlete-tracker/callback,http://localhost:3001/auth/callback,http://localhost:3000/auth/callback',
    'http://localhost:8080,http://localhost:3001,http://localhost:3000',
    'athlete:read,performance:read,workouts:read,assessments:read,athlete:contact',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",3600.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",7200.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",600.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",600.000000000]}'
);

-- Create index for faster client lookups (if not already exists)
CREATE INDEX IF NOT EXISTS idx_oauth2_registered_client_client_id ON oauth2_registered_client(client_id);

-- Add comment explaining the client configuration
COMMENT ON TABLE oauth2_registered_client IS 'OAuth2 registered clients for Prospecto Performance application. Includes production and development clients.';