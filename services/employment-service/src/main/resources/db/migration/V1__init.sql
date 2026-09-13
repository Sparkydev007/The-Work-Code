-- Employment Service schema (schema: employment-service)
CREATE TABLE IF NOT EXISTS employment_records (
    id               UUID PRIMARY KEY,
    employee_number  VARCHAR(64)  NOT NULL,
    employer_name    VARCHAR(200) NOT NULL,
    employer_code    VARCHAR(32),
    job_title        VARCHAR(160),
    department       VARCHAR(120),
    status           VARCHAR(20)  NOT NULL,
    employment_type  VARCHAR(30),
    hire_date        DATE         NOT NULL,
    termination_date DATE,
    work_location    VARCHAR(200),
    source           VARCHAR(200) NOT NULL DEFAULT 'Synthetic employer payroll feed',
    confidence       NUMERIC(4,3) NOT NULL DEFAULT 0.95,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_emp_records_employee ON employment_records (employee_number);
CREATE INDEX IF NOT EXISTS idx_emp_records_employer ON employment_records (employer_code);
CREATE INDEX IF NOT EXISTS idx_emp_records_hire     ON employment_records (hire_date);

CREATE TABLE IF NOT EXISTS employment_verifications (
    id                UUID PRIMARY KEY,
    employee_number   VARCHAR(64)  NOT NULL,
    employer_name     VARCHAR(200),
    employment_status VARCHAR(20),
    job_title         VARCHAR(160),
    department        VARCHAR(120),
    hire_date         DATE,
    termination_date  DATE,
    result            VARCHAR(20)  NOT NULL, -- VERIFIED | REVIEW | NOT_VERIFIED | NO_RECORD
    confidence        NUMERIC(5,2) NOT NULL,
    lookback_months   INTEGER,
    details           TEXT,
    request_id        VARCHAR(64),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_emp_verif_employee ON employment_verifications (employee_number);
CREATE INDEX IF NOT EXISTS idx_emp_verif_result   ON employment_verifications (result);
CREATE INDEX IF NOT EXISTS idx_emp_verif_created  ON employment_verifications (created_at);
