-- Remove custom client_applications table and related structures
-- We'll use Spring Security's built-in oauth2_registered_client table instead

-- Drop indexes first
DROP INDEX IF EXISTS idx_client_applications_client_id;
DROP INDEX IF EXISTS idx_client_applications_status;
DROP INDEX IF EXISTS idx_client_applications_created_by;

-- Drop the custom client_applications table
DROP TABLE IF EXISTS client_applications;

-- Note: We keep the oauth2_registered_client table as it's Spring Security's standard table
-- Note: We keep the oauth2_scopes table as it's still used for scope management
-- Note: We keep the oauth2_access_logs table for audit purposes

-- Update the oauth2_registered_client table structure to ensure it matches Spring's expected schema
-- (This is mostly already correct, but let's ensure consistency)

-- Add any missing columns or constraints if needed
-- Spring's JdbcRegisteredClientRepository expects specific column names and types
ALTER TABLE oauth2_registered_client 
    ALTER COLUMN client_settings TYPE TEXT,
    ALTER COLUMN token_settings TYPE TEXT;

-- Ensure proper indexes exist for Spring's repository
CREATE INDEX IF NOT EXISTS idx_oauth2_registered_client_client_id ON oauth2_registered_client(client_id);

-- Remove the default client that was inserted in V9 (we'll create proper clients through the API)
DELETE FROM oauth2_registered_client WHERE id = 'default-client-id';

-- Add a comment to document the simplified approach
COMMENT ON TABLE oauth2_registered_client IS 'Spring Security OAuth2 registered clients - simplified to use only Spring''s built-in client management';