CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,

    tenant_id     BIGINT       NOT NULL
        REFERENCES tenants (id),

    username      VARCHAR(100) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    role          VARCHAR(30)  NOT NULL,

    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'MANAGER', 'OPERATOR')),

    CONSTRAINT uq_users_tenant_username
        UNIQUE (tenant_id, username)
);

CREATE INDEX idx_users_tenant_id
    ON users (tenant_id);