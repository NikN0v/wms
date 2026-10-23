CREATE TABLE products
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL REFERENCES tenants (id),

    sku         VARCHAR(100) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,

    unit        VARCHAR(20)  NOT NULL DEFAULT 'PCS',
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_products_tenant_sku UNIQUE (tenant_id, sku),
    CONSTRAINT uq_products_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT chk_products_unit
        CHECK (unit IN ('PCS', 'KG', 'G', 'L', 'ML', 'BOX'))
);

CREATE INDEX idx_products_tenant_id
    ON products (tenant_id);

CREATE INDEX idx_products_tenant_active
    ON products (tenant_id, is_active);