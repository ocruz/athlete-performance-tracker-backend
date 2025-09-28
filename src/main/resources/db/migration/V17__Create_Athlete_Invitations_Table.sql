-- Create Athlete Invitations Table
-- This migration adds the athlete_invitations table for managing athlete invitation tokens
-- and linking athlete profiles to user accounts through an invitation-based registration flow.

CREATE TABLE athlete_invitations (
    id BIGSERIAL PRIMARY KEY,
    athlete_id BIGINT NOT NULL,
    invitation_token VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_by BIGINT,
    
    CONSTRAINT fk_athlete_invitations_athlete 
        FOREIGN KEY (athlete_id) REFERENCES athletes(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_athlete_invitations_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) 
        ON DELETE SET NULL
);

-- Create indexes for performance
CREATE INDEX idx_athlete_invitations_athlete_id ON athlete_invitations(athlete_id);
CREATE INDEX idx_athlete_invitations_token ON athlete_invitations(invitation_token);
CREATE INDEX idx_athlete_invitations_email ON athlete_invitations(email);
CREATE INDEX idx_athlete_invitations_expires_at ON athlete_invitations(expires_at);
CREATE INDEX idx_athlete_invitations_is_used ON athlete_invitations(is_used);
CREATE INDEX idx_athlete_invitations_created_at ON athlete_invitations(created_at);

-- Composite index for finding active invitations
CREATE INDEX idx_athlete_invitations_active 
    ON athlete_invitations(athlete_id, is_used, expires_at)
    WHERE is_used = FALSE;

-- Add comments for documentation
COMMENT ON TABLE athlete_invitations IS 
    'Stores invitation tokens for athletes to create user accounts and link them to their profiles. Part of the invitation-based registration system.';

COMMENT ON COLUMN athlete_invitations.athlete_id IS 
    'Foreign key to the athletes table - the athlete profile this invitation is for';

COMMENT ON COLUMN athlete_invitations.invitation_token IS 
    'Unique UUID token used in the invitation URL for secure registration';

COMMENT ON COLUMN athlete_invitations.email IS 
    'Email address where the invitation was sent (should match athlete.email)';

COMMENT ON COLUMN athlete_invitations.expires_at IS 
    'When this invitation expires (default 7 days from creation)';

COMMENT ON COLUMN athlete_invitations.is_used IS 
    'Whether this invitation has been used to create an account';

COMMENT ON COLUMN athlete_invitations.used_at IS 
    'Timestamp when the invitation was used (NULL if not used)';

COMMENT ON COLUMN athlete_invitations.created_by IS 
    'User ID of the coach/admin who created this invitation (optional)';

-- Add constraint to ensure expires_at is in the future when created
ALTER TABLE athlete_invitations 
    ADD CONSTRAINT chk_athlete_invitations_expires_future 
    CHECK (expires_at > created_at);

-- Add constraint to ensure used_at is set when is_used is true
-- Note: This is a soft constraint since we can't enforce it perfectly in PostgreSQL
-- The application logic should ensure this consistency