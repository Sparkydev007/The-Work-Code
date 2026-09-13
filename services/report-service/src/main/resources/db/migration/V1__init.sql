-- Report Service schema (schema: report-service)
CREATE TABLE IF NOT EXISTS reports (
    id                UUID PRIMARY KEY,
    report_code       VARCHAR(40) UNIQUE,
    verification_id   VARCHAR(64),
    verification_code VARCHAR(40),
    report_type       VARCHAR(40)  NOT NULL,
    applicant_name    VARCHAR(200),
    employer_name     VARCHAR(200),
    result            VARCHAR(40),
    confidence        NUMERIC(5,2),
    generated_by      VARCHAR(120),
    generated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    pdf_bytes         BYTEA,
    access_count      INTEGER      NOT NULL DEFAULT 0,
    content           TEXT
);

CREATE INDEX IF NOT EXISTS idx_reports_verification ON reports (verification_id);
CREATE INDEX IF NOT EXISTS idx_reports_created      ON reports (generated_at DESC);
