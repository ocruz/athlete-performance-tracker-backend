-- Add foreign key constraint for coaches table
ALTER TABLE coaches 
ADD CONSTRAINT fk_coaches_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add check constraints for data validation
ALTER TABLE coaches 
ADD CONSTRAINT chk_coaches_years_experience 
CHECK (years_experience >= 0 AND years_experience <= 50);

-- Add email format validation (PostgreSQL regex)
ALTER TABLE coaches 
ADD CONSTRAINT chk_coaches_email_format 
CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

-- Add constraint for contact method values
ALTER TABLE coaches 
ADD CONSTRAINT chk_coaches_contact_method 
CHECK (preferred_contact_method IS NULL OR preferred_contact_method IN ('EMAIL', 'PHONE', 'TEXT', 'IN_PERSON'));

-- Add additional indexes for common search patterns
CREATE INDEX idx_coaches_specializations_gin ON coaches USING gin(to_tsvector('english', specializations)) 
WHERE specializations IS NOT NULL AND specializations != '';

CREATE INDEX idx_coaches_preferred_sports ON coaches(preferred_sports) 
WHERE preferred_sports IS NOT NULL;

CREATE INDEX idx_coaches_years_experience ON coaches(years_experience) 
WHERE years_experience IS NOT NULL;