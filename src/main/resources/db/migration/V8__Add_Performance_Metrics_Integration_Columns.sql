-- Migration to add performance metrics integration columns
-- This migration adds the new columns for integrating performance metrics with assessments and workouts

-- Add new columns to performance_metrics table
ALTER TABLE performance_metrics ADD COLUMN IF NOT EXISTS source_type VARCHAR(20) DEFAULT 'MANUAL' NOT NULL;
ALTER TABLE performance_metrics ADD COLUMN IF NOT EXISTS source_assessment_result_id BIGINT;
ALTER TABLE performance_metrics ADD COLUMN IF NOT EXISTS source_workout_exercise_id BIGINT;
ALTER TABLE performance_metrics ADD COLUMN IF NOT EXISTS is_personal_record BOOLEAN DEFAULT FALSE NOT NULL;

-- Add foreign key constraints
ALTER TABLE performance_metrics ADD CONSTRAINT fk_performance_metrics_assessment_result 
    FOREIGN KEY (source_assessment_result_id) REFERENCES assessment_results(id);

ALTER TABLE performance_metrics ADD CONSTRAINT fk_performance_metrics_workout_exercise 
    FOREIGN KEY (source_workout_exercise_id) REFERENCES workout_exercises(id);

-- Add new column to assessments table
ALTER TABLE assessments ADD COLUMN IF NOT EXISTS generate_performance_metric BOOLEAN DEFAULT FALSE NOT NULL;

-- Add new column to workout_exercises table
ALTER TABLE workout_exercises ADD COLUMN IF NOT EXISTS is_pr BOOLEAN DEFAULT FALSE NOT NULL;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_performance_metrics_source_type ON performance_metrics(source_type);
CREATE INDEX IF NOT EXISTS idx_performance_metrics_is_pr ON performance_metrics(is_personal_record);
CREATE INDEX IF NOT EXISTS idx_performance_metrics_source_assessment ON performance_metrics(source_assessment_result_id);
CREATE INDEX IF NOT EXISTS idx_performance_metrics_source_workout ON performance_metrics(source_workout_exercise_id);
CREATE INDEX IF NOT EXISTS idx_assessments_generate_metric ON assessments(generate_performance_metric);
CREATE INDEX IF NOT EXISTS idx_workout_exercises_is_pr ON workout_exercises(is_pr);

-- Update some existing assessments to auto-generate performance metrics
UPDATE assessments SET generate_performance_metric = TRUE 
WHERE name ILIKE '%1-Rep Max%' OR name ILIKE '%Bench Press%' OR name ILIKE '%Squat%' OR name ILIKE '%Deadlift%' 
   OR name ILIKE '%Vertical Jump%' OR name ILIKE '%Broad Jump%' OR name ILIKE '%40%yard%';