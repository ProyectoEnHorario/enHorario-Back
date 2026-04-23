ALTER TABLE establishments
    ADD COLUMN IF NOT EXISTS average_wait_time_rating NUMERIC(2,1);