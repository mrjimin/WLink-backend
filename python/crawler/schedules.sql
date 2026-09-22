CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_period BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul'),

    CONSTRAINT chk_schedules_date
       CHECK (end_date >= start_date)
);

CREATE INDEX idx_schedules_date
    ON schedules(start_date, end_date);
