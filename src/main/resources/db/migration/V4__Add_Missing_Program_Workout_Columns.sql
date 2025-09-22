-- Add missing columns to program_workouts table

-- Add missing columns to match ProgramWorkout entity
ALTER TABLE program_workouts ADD COLUMN workout_type VARCHAR(50) NOT NULL DEFAULT 'MIXED';
ALTER TABLE program_workouts ADD COLUMN estimated_duration INTEGER;
ALTER TABLE program_workouts ADD COLUMN notes TEXT;
ALTER TABLE program_workouts ADD COLUMN warmup_instructions TEXT;
ALTER TABLE program_workouts ADD COLUMN cooldown_instructions TEXT;
ALTER TABLE program_workouts ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE program_workouts ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add check constraint for workout_type
ALTER TABLE program_workouts ADD CONSTRAINT program_workouts_workout_type_check 
    CHECK (workout_type IN (
        'STRENGTH', 'CARDIO', 'HIIT', 'PLYOMETRIC', 'MOBILITY', 
        'SPORT_SPECIFIC', 'RECOVERY', 'ASSESSMENT', 'MIXED'
    ));