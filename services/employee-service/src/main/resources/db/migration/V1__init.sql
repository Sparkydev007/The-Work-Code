-- Employee Service schema (schema: employee-service)
CREATE TABLE IF NOT EXISTS employees (
    id                      UUID PRIMARY KEY,
    employee_number         VARCHAR(64)  NOT NULL,
    name                    VARCHAR(160) NOT NULL,
    date_of_birth           DATE,
    email                   VARCHAR(200),
    phone                   VARCHAR(40),
    employer_name           VARCHAR(200),
    employer_code           VARCHAR(32),
    status                  VARCHAR(20)  NOT NULL,
    department              VARCHAR(120),
    job_title               VARCHAR(160),
    employment_type         VARCHAR(30),
    hire_date               DATE,
    termination_date        DATE,
    work_location           VARCHAR(200),
    monthly_base_income_inr BIGINT,
    pay_frequency           VARCHAR(20),
    scenario_tag            VARCHAR(40),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_employees_employee_number UNIQUE (employee_number)
);

CREATE INDEX IF NOT EXISTS idx_employees_employer_code ON employees (employer_code);
CREATE INDEX IF NOT EXISTS idx_employees_status        ON employees (status);
CREATE INDEX IF NOT EXISTS idx_employees_email         ON employees (email);
CREATE INDEX IF NOT EXISTS idx_employees_name          ON employees (name);
CREATE INDEX IF NOT EXISTS idx_employees_created_at    ON employees (created_at);
