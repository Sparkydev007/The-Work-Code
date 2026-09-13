-- Income Service schema (schema: income-service)
CREATE TABLE IF NOT EXISTS income_records (
    id               UUID PRIMARY KEY,
    employee_number  VARCHAR(64) NOT NULL,
    employer_name    VARCHAR(200),
    annual_base_inr  BIGINT NOT NULL,
    annual_overtime_inr BIGINT NOT NULL DEFAULT 0,
    annual_bonus_inr BIGINT NOT NULL DEFAULT 0,
    annual_commission_inr BIGINT NOT NULL DEFAULT 0,
    annual_other_inr BIGINT NOT NULL DEFAULT 0,
    annual_total_inr BIGINT NOT NULL,
    monthly_total_inr BIGINT NOT NULL,
    pay_frequency    VARCHAR(20) NOT NULL,
    confidence       NUMERIC(4,3) NOT NULL DEFAULT 0.95,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_income_employee UNIQUE (employee_number)
);

CREATE INDEX IF NOT EXISTS idx_income_employee ON income_records (employee_number);

CREATE TABLE IF NOT EXISTS pay_periods (
    id               UUID PRIMARY KEY,
    employee_number  VARCHAR(64) NOT NULL,
    pay_date         DATE        NOT NULL,
    period_start     DATE        NOT NULL,
    period_end       DATE        NOT NULL,
    gross_pay_inr    BIGINT      NOT NULL,
    net_pay_inr      BIGINT      NOT NULL,
    base_pay_inr     BIGINT      NOT NULL,
    overtime_inr     BIGINT      NOT NULL DEFAULT 0,
    bonus_inr        BIGINT      NOT NULL DEFAULT 0,
    commission_inr   BIGINT      NOT NULL DEFAULT 0,
    hours_worked     NUMERIC(6,2) NOT NULL,
    deductions_inr   BIGINT      NOT NULL,
    pay_frequency    VARCHAR(20) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_pay_periods_employee ON pay_periods (employee_number);
CREATE INDEX IF NOT EXISTS idx_pay_periods_pay_date ON pay_periods (pay_date DESC);

CREATE TABLE IF NOT EXISTS income_verifications (
    id                UUID PRIMARY KEY,
    employee_number   VARCHAR(64) NOT NULL,
    employer_name     VARCHAR(200),
    result            VARCHAR(20) NOT NULL, -- VERIFIED | REVIEW | NOT_VERIFIED | NO_RECORD
    confidence        NUMERIC(5,2) NOT NULL,
    lookback_months   INTEGER,
    annualized_income BIGINT,
    monthly_income    BIGINT,
    pay_frequency     VARCHAR(20),
    details           TEXT,
    request_id        VARCHAR(64),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_income_verif_employee ON income_verifications (employee_number);
CREATE INDEX IF NOT EXISTS idx_income_verif_created  ON income_verifications (created_at);
