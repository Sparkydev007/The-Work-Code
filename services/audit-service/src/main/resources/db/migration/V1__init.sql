-- Audit Service schema (schema: audit-service)
CREATE TABLE IF NOT EXISTS audit_events (
    id          UUID PRIMARY KEY,
    event_time  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actor       VARCHAR(120),
    actor_role  VARCHAR(30),
    action      VARCHAR(60)  NOT NULL, -- LOGIN | CREATE_VERIFICATION | VIEW_VERIFICATION | DOWNLOAD_REPORT | ...
    resource_type VARCHAR(60),
    resource_id VARCHAR(80),
    request_id  VARCHAR(64),
    result      VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
    detail      VARCHAR(600),
    org_id      VARCHAR(40)
);

CREATE INDEX IF NOT EXISTS idx_audit_time    ON audit_events (event_time DESC);
CREATE INDEX IF NOT EXISTS idx_audit_actor   ON audit_events (actor);
CREATE INDEX IF NOT EXISTS idx_audit_action  ON audit_events (action);
CREATE INDEX IF NOT EXISTS idx_audit_request ON audit_events (request_id);
