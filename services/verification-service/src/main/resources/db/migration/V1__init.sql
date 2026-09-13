-- Verification Service schema (schema: verification-service)
CREATE TABLE IF NOT EXISTS verification_requests (
    id                   UUID PRIMARY KEY,
    verification_code    VARCHAR(40) UNIQUE,
    idempotency_key      VARCHAR(80) UNIQUE,
    organization_id      VARCHAR(40),
    requested_by         VARCHAR(120),
    verification_type    VARCHAR(40)  NOT NULL, -- EMPLOYMENT | INCOME | EMPLOYMENT_AND_INCOME | IDENTITY | EMPLOYMENT_HISTORY
    purpose              VARCHAR(40)  NOT NULL, -- LOAN | MORTGAGE | AUTO_FINANCE | EMPLOYMENT_SCREENING | HOUSING | BENEFITS | OTHER
    applicant_name       VARCHAR(200) NOT NULL,
    applicant_email      VARCHAR(200),
    applicant_dob        DATE,
    employee_number      VARCHAR(64),
    employer_name        VARCHAR(200),
    lookback_months      INTEGER,
    status               VARCHAR(30)  NOT NULL, -- state machine
    result               VARCHAR(30),
    confidence           NUMERIC(5,2),
    risk_score           INTEGER,
    risk_band            VARCHAR(10),
    identity_ref         VARCHAR(64),
    employment_ref       VARCHAR(64),
    income_ref           VARCHAR(64),
    report_ref           VARCHAR(64),
    failure_reason       TEXT,
    attributes           TEXT,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at         TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_verif_status    ON verification_requests (status);
CREATE INDEX IF NOT EXISTS idx_verif_type      ON verification_requests (verification_type);
CREATE INDEX IF NOT EXISTS idx_verif_employee  ON verification_requests (employee_number);
CREATE INDEX IF NOT EXISTS idx_verif_org       ON verification_requests (organization_id);
CREATE INDEX IF NOT EXISTS idx_verif_created   ON verification_requests (created_at DESC);

CREATE TABLE IF NOT EXISTS verification_workflow_events (
    id            UUID PRIMARY KEY,
    request_id    UUID        NOT NULL,
    step          VARCHAR(40) NOT NULL,
    status        VARCHAR(20) NOT NULL, -- PENDING | RUNNING | DONE | FAILED | SKIPPED
    detail        VARCHAR(400),
    occurred_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_wf_events_request ON verification_workflow_events (request_id);

CREATE TABLE IF NOT EXISTS disputes (
    id              UUID PRIMARY KEY,
    dispute_code    VARCHAR(40) UNIQUE,
    verification_id UUID,
    employee_number VARCHAR(64),
    field_name      VARCHAR(80)  NOT NULL,
    reported_value  VARCHAR(300) NOT NULL,
    claimed_value   VARCHAR(300) NOT NULL,
    explanation     VARCHAR(1000),
    status          VARCHAR(20)  NOT NULL, -- SUBMITTED | UNDER_REVIEW | RESOLVED | REJECTED
    resolution      VARCHAR(1000),
    submitted_by    VARCHAR(120),
    reviewed_by     VARCHAR(120),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_disputes_status ON disputes (status);

CREATE TABLE IF NOT EXISTS manual_verifications (
    id               UUID PRIMARY KEY,
    verification_id  UUID,
    employee_number  VARCHAR(64),
    employer_name    VARCHAR(200),
    assigned_analyst VARCHAR(120),
    employer_contact VARCHAR(200),
    contact_method   VARCHAR(40),
    requested_attributes TEXT,
    notes            VARCHAR(1000),
    status           VARCHAR(30) NOT NULL, -- PENDING | IN_PROGRESS | AWAITING_RESPONSE | COMPLETED | FAILED
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at     TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_manual_status ON manual_verifications (status);

CREATE TABLE IF NOT EXISTS organization_credentialing (
    id                UUID PRIMARY KEY,
    organization_id   VARCHAR(40) NOT NULL UNIQUE,
    organization_name VARCHAR(200) NOT NULL,
    verification_purpose VARCHAR(200),
    credential_status VARCHAR(40) NOT NULL, -- PENDING_CREDENTIALING | CREDENTIALED | SUSPENDED
    credentialed      BOOLEAN NOT NULL DEFAULT false,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
