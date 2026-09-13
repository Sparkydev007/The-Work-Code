-- Batch Service schema (schema: batch-service)
CREATE TABLE IF NOT EXISTS batch_jobs (
    id             UUID PRIMARY KEY,
    batch_code     VARCHAR(40) UNIQUE,
    file_name      VARCHAR(200),
    total_rows     INTEGER NOT NULL DEFAULT 0,
    processed_rows INTEGER NOT NULL DEFAULT 0,
    verified_rows  INTEGER NOT NULL DEFAULT 0,
    review_rows    INTEGER NOT NULL DEFAULT 0,
    failed_rows    INTEGER NOT NULL DEFAULT 0,
    status         VARCHAR(20) NOT NULL, -- UPLOADED | VALIDATING | PROCESSING | COMPLETED | FAILED
    created_by     VARCHAR(120),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at   TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_batch_status ON batch_jobs (status);

CREATE TABLE IF NOT EXISTS batch_items (
    id             UUID PRIMARY KEY,
    batch_id       UUID        NOT NULL,
    row_number     INTEGER     NOT NULL,
    employee_name  VARCHAR(200),
    employee_number VARCHAR(64),
    employer       VARCHAR(200),
    verification_type VARCHAR(40),
    status         VARCHAR(20) NOT NULL, -- VALID | INVALID | VERIFIED | REVIEW | FAILED
    failure_reason VARCHAR(300),
    result         VARCHAR(40),
    confidence     NUMERIC(5,2),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_batch_items_batch ON batch_items (batch_id);
