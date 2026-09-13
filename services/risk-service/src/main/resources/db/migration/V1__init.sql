-- Risk Service schema (schema: risk-service)
CREATE TABLE IF NOT EXISTS risk_assessments (
    id               UUID PRIMARY KEY,
    verification_id  VARCHAR(64),
    employee_number  VARCHAR(64),
    applicant_name   VARCHAR(200),
    employer_name    VARCHAR(200),
    score            INTEGER     NOT NULL,
    band             VARCHAR(10) NOT NULL, -- LOW | MEDIUM | HIGH | CRITICAL
    signals          JSONB,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_risk_verification ON risk_assessments (verification_id);
CREATE INDEX IF NOT EXISTS idx_risk_band         ON risk_assessments (band);
CREATE INDEX IF NOT EXISTS idx_risk_created      ON risk_assessments (created_at);

CREATE TABLE IF NOT EXISTS risk_anomalies (
    id            UUID PRIMARY KEY,
    rule_code     VARCHAR(60)  NOT NULL,
    severity      VARCHAR(12)  NOT NULL, -- INFO | LOW | MEDIUM | HIGH | CRITICAL
    title         VARCHAR(200) NOT NULL,
    explanation   VARCHAR(1000) NOT NULL,
    employee_number VARCHAR(64),
    employer_name VARCHAR(200),
    verification_id VARCHAR(64),
    recommended_action VARCHAR(300),
    status        VARCHAR(20)  NOT NULL DEFAULT 'OPEN', -- OPEN | ACKNOWLEDGED | RESOLVED
    detected_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_anomalies_status ON risk_anomalies (status);
CREATE INDEX IF NOT EXISTS idx_anomalies_severity ON risk_anomalies (severity);
