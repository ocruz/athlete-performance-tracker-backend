-- Fix performance_metrics table to match entity expectations
-- Add missing test_date column and update data

-- Update existing data to use test_date
UPDATE performance_metrics SET test_date = CURRENT_TIMESTAMP WHERE test_date IS NULL;

-- Make recorded_by_id nullable to match entity
ALTER TABLE performance_metrics ALTER COLUMN recorded_by_id DROP NOT NULL;
ALTER TABLE performance_metrics ALTER COLUMN unit DROP NOT NULL;