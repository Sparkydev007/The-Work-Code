-- Notification Service schema (schema: notification-service)
CREATE TABLE IF NOT EXISTS webhooks (
    id          UUID PRIMARY KEY,
    url         VARCHAR(400) NOT NULL,
    description VARCHAR(300),
    events      VARCHAR(400) NOT NULL, -- comma-separated event types
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | PAUSED | REVOKED
    secret_hint VARCHAR(20),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS webhook_deliveries (
    id          UUID PRIMARY KEY,
    webhook_id  UUID        NOT NULL,
    event_type  VARCHAR(60) NOT NULL,
    payload     TEXT,
    status      VARCHAR(20) NOT NULL, -- SUCCESS | FAILED | RETRYING
    attempt     INTEGER     NOT NULL DEFAULT 1,
    response_code INTEGER,
    duration_ms INTEGER,
    delivered_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_deliveries_webhook ON webhook_deliveries (webhook_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_status  ON webhook_deliveries (status);

CREATE TABLE IF NOT EXISTS notifications (
    id          UUID PRIMARY KEY,
    event_type  VARCHAR(60) NOT NULL,
    title       VARCHAR(200) NOT NULL,
    body        VARCHAR(600),
    severity    VARCHAR(12)  NOT NULL DEFAULT 'INFO',
    read        BOOLEAN      NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
