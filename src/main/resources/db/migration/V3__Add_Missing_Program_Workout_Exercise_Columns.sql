-- Add missing columns to program_workout_exercises table

-- Add missing columns to match ProgramWorkoutExercise entity
ALTER TABLE program_workout_exercises ADD COLUMN weight DOUBLE PRECISION;
ALTER TABLE program_workout_exercises ADD COLUMN distance DOUBLE PRECISION;
ALTER TABLE program_workout_exercises ADD COLUMN time INTEGER;
ALTER TABLE program_workout_exercises ADD COLUMN coach_instructions TEXT;
ALTER TABLE program_workout_exercises ADD COLUMN progression_type VARCHAR(50);
ALTER TABLE program_workout_exercises ADD COLUMN progression_value DOUBLE PRECISION;
ALTER TABLE program_workout_exercises ADD COLUMN is_superset BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE program_workout_exercises ADD COLUMN superset_group INTEGER;
ALTER TABLE program_workout_exercises ADD COLUMN is_dropset BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE program_workout_exercises ADD COLUMN is_failure BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE program_workout_exercises ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add check constraint for progression_type
ALTER TABLE program_workout_exercises ADD CONSTRAINT program_workout_exercises_progression_type_check 
    CHECK (progression_type IS NULL OR progression_type IN (
        'WEIGHT', 'REPS', 'SETS', 'TIME', 'DISTANCE', 'REST_DECREASE', 'INTENSITY', 'NONE'
    ));