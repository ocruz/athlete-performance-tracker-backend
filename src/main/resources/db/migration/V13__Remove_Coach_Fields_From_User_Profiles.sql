-- Remove coach-specific columns from user_profiles table since they are now in coaches table
ALTER TABLE user_profiles DROP COLUMN IF EXISTS office_location;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS years_experience;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS certifications;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS specializations;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS coaching_philosophy;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS preferred_sports;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS preferred_contact_method;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS availability_hours;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS bio;

-- Add new universal preference columns for all user types
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(10) DEFAULT 'en';
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS timezone VARCHAR(50) DEFAULT 'UTC';
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS profile_visibility VARCHAR(20) DEFAULT 'PUBLIC';

-- Add check constraint for profile visibility
ALTER TABLE user_profiles 
ADD CONSTRAINT chk_user_profiles_visibility 
CHECK (profile_visibility IN ('PUBLIC', 'PRIVATE', 'TEAM_ONLY'));