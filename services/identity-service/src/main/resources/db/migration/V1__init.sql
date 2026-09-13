-- Identity Service schema (schema: identity-service)
CREATE TABLE IF NOT EXISTS identity_verifications (
    id                      UUID PRIMARY KEY,
    input_name              VARCHAR(200),
    input_employee_number   VARCHAR(64),
    input_date_of_birth     DATE,
    input_email             VARCHAR(200),
    input_employer          VARCHAR(200),
    result                  VARCHAR(20)  NOT NULL, -- MATCH | NO_MATCH | NO_HIT
    confidence              NUMERIC(5,2) NOT NULL,
    name_score              NUMERIC(5,2),
    employer_score          NUMERIC(5,2),
    employee_id_score       NUMERIC(5,2),
    email_score             NUMERIC(5,2),
    matched_employee_number VARCHAR(64),
    breakdown               TEXT,
    request_id              VARCHAR(64),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_identity_result  ON identity_verifications (result);
CREATE INDEX IF NOT EXISTS idx_identity_created ON identity_verifications (created_at);
CREATE INDEX IF NOT EXISTS idx_identity_matched ON identity_verifications (matched_employee_number);
