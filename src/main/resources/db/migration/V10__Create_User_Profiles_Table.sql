-- Create user_profiles table for universal user preferences
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    phone VARCHAR(20),
    profile_photo_url VARCHAR(500),
    notification_preferences TEXT,
    preferred_language VARCHAR(10),
    timezone VARCHAR(50),
    profile_visibility VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_profiles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE UNIQUE INDEX idx_user_profiles_user_id_unique ON user_profiles(user_id);

-- Add comments for documentation
COMMENT ON TABLE user_profiles IS 'Universal user preferences and profile data for all user types';
COMMENT ON COLUMN user_profiles.user_id IS 'Foreign key reference to users table';
COMMENT ON COLUMN user_profiles.notification_preferences IS 'JSON string containing notification preferences';
COMMENT ON COLUMN user_profiles.profile_visibility IS 'Controls who can see this user profile (public, private, team)';