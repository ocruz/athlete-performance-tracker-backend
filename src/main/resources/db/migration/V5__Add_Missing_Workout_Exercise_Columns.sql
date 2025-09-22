-- Add missing columns to workout_exercises table

-- Add planned values columns
ALTER TABLE workout_exercises ADD COLUMN planned_sets INTEGER;
ALTER TABLE workout_exercises ADD COLUMN planned_reps INTEGER;
ALTER TABLE workout_exercises ADD COLUMN planned_weight DOUBLE PRECISION;
ALTER TABLE workout_exercises ADD COLUMN planned_distance DOUBLE PRECISION;
ALTER TABLE workout_exercises ADD COLUMN planned_time INTEGER;
ALTER TABLE workout_exercises ADD COLUMN planned_rest_time INTEGER;
ALTER TABLE workout_exercises ADD COLUMN planned_intensity DOUBLE PRECISION;

-- Add actual performed values columns
ALTER TABLE workout_exercises ADD COLUMN actual_sets INTEGER;
ALTER TABLE workout_exercises ADD COLUMN actual_reps INTEGER;
ALTER TABLE workout_exercises ADD COLUMN actual_weight DOUBLE PRECISION;
ALTER TABLE workout_exercises ADD COLUMN actual_distance DOUBLE PRECISION;
ALTER TABLE workout_exercises ADD COLUMN actual_time INTEGER;
ALTER TABLE workout_exercises ADD COLUMN actual_rest_time INTEGER;
ALTER TABLE workout_exercises ADD COLUMN actual_intensity DOUBLE PRECISION;

-- Add additional fields
ALTER TABLE workout_exercises ADD COLUMN is_from_program BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE workout_exercises ADD COLUMN completion_status VARCHAR(50) NOT NULL DEFAULT 'PLANNED';
ALTER TABLE workout_exercises ADD COLUMN rpe INTEGER;
ALTER TABLE workout_exercises ADD COLUMN program_workout_exercise_id BIGINT;

-- Add check constraints
ALTER TABLE workout_exercises ADD CONSTRAINT workout_exercises_completion_status_check 
    CHECK (completion_status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'MODIFIED', 'SKIPPED', 'FAILED'));

ALTER TABLE workout_exercises ADD CONSTRAINT workout_exercises_rpe_check 
    CHECK (rpe IS NULL OR (rpe >= 1 AND rpe <= 10));

-- Add foreign key constraint
ALTER TABLE workout_exercises ADD CONSTRAINT fk_workout_exercises_program_workout_exercise 
    FOREIGN KEY (program_workout_exercise_id) REFERENCES program_workout_exercises(id);

-- Also fix workouts table - add missing program_workout_id column
ALTER TABLE workouts ADD COLUMN program_workout_id BIGINT;
ALTER TABLE workouts ADD CONSTRAINT fk_workouts_program_workout 
    FOREIGN KEY (program_workout_id) REFERENCES program_workouts(id);

-- Make workout name nullable to match entity
ALTER TABLE workouts ALTER COLUMN name DROP NOT NULL;