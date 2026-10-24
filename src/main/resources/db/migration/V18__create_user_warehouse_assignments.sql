ALTER TABLE users
    ADD CONSTRAINT uq_users_id_tenant UNIQUE (id, tenant_id);

CREATE TABLE user_warehouse_assignments
(
    tenant_id    BIGINT      NOT NULL,
    user_id      BIGINT      NOT NULL,
    warehouse_id BIGINT      NOT NULL,
    assigned_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_warehouse_assignments
        PRIMARY KEY (tenant_id, user_id, warehouse_id),
    CONSTRAINT fk_user_warehouse_assignments_user_tenant
        FOREIGN KEY (user_id, tenant_id)
            REFERENCES users (id, tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_warehouse_assignments_warehouse_tenant
        FOREIGN KEY (warehouse_id, tenant_id)
            REFERENCES warehouses (id, tenant_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_warehouse_assignments_warehouse
    ON user_warehouse_assignments (tenant_id, warehouse_id);
