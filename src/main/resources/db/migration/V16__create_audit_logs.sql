CREATE TABLE audit_logs
(
    id          BIGSERIAL PRIMARY KEY,

    tenant_id   BIGINT       NOT NULL
        REFERENCES tenants (id),

    user_id     BIGINT,

    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   BIGINT,

    details     JSONB,

    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_tenant_created_at
    ON audit_logs (tenant_id, created_at DESC);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs (tenant_id, entity_type, entity_id);

CREATE INDEX idx_audit_logs_action
    ON audit_logs (tenant_id, action, created_at DESC);
