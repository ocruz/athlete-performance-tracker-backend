-- Create basic coach records for existing coach users
-- Note: Only basic info available from user_profiles, coach-specific fields will be NULL initially
INSERT INTO coaches (
    first_name, 
    last_name, 
    email, 
    phone,
    profile_photo_url,
    created_at,
    updated_at,
    user_id
)
SELECT 
    u.first_name,
    u.last_name,
    u.email,
    up.phone,
    up.profile_photo_url,
    COALESCE(up.created_at, CURRENT_TIMESTAMP),
    COALESCE(up.updated_at, CURRENT_TIMESTAMP),
    up.user_id
FROM user_profiles up
JOIN users u ON up.user_id = u.id
WHERE u.role = 'COACH'
  AND up.user_id IS NOT NULL;