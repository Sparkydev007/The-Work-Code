-- Employer Service schema (schema: employer-service)
CREATE TABLE IF NOT EXISTS employers (
    id                UUID PRIMARY KEY,
    employer_code     VARCHAR(32)  NOT NULL,
    name              VARCHAR(200) NOT NULL,
    industry          VARCHAR(120),
    city              VARCHAR(120),
    employee_count    INTEGER,
    trust_status      VARCHAR(40)  NOT NULL,
    contributor_since DATE,
    last_data_update  DATE,
    data_source       VARCHAR(200) NOT NULL DEFAULT 'Synthetic employer payroll feed',
    refresh_frequency VARCHAR(60)  NOT NULL DEFAULT 'Every pay period',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_employers_code UNIQUE (employer_code)
);

CREATE INDEX IF NOT EXISTS idx_employers_industry ON employers (industry);
CREATE INDEX IF NOT EXISTS idx_employers_trust    ON employers (trust_status);
CREATE INDEX IF NOT EXISTS idx_employers_name     ON employers (name);
