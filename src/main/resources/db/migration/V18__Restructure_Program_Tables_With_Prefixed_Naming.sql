-- V18: Restructure Program Tables with Prefixed Naming
-- Purpose: Rename program-related tables to use clear prefixed naming that distinguishes templates from instances

-- =====================================================================
-- STEP 1: Rename Instance Tables (Actual Executions)
-- =====================================================================

-- Rename actual workout sessions table
ALTER TABLE workouts RENAME TO athlete_workouts;

-- Rename actual workout exercises table
ALTER TABLE workout_exercises RENAME TO athlete_workout_exercises;

-- =====================================================================
-- STEP 2: Rename Tracking Tables
-- =====================================================================

-- Rename program progress tracking table
ALTER TABLE program_progress RENAME TO athlete_exercise_completions;