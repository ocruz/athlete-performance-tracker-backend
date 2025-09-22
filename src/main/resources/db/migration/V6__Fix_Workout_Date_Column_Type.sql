-- Fix workout_date column type to match entity expectation
ALTER TABLE workouts ALTER COLUMN workout_date SET DATA TYPE TIMESTAMP;