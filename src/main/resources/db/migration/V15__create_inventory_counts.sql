CREATE TABLE inventory_counts
(
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL REFERENCES tenants (id),
    warehouse_id    BIGINT       NOT NULL REFERENCES warehouses (id),
    document_number VARCHAR(100) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    posted_at       TIMESTAMPTZ,

    CONSTRAINT uq_inventory_counts_tenant_document
        UNIQUE (tenant_id, document_number),

    CONSTRAINT chk_inventory_count_status
        CHECK (status IN ('DRAFT', 'POSTED', 'CANCELLED'))
);

CREATE INDEX idx_inventory_counts_warehouse
    ON inventory_counts (tenant_id, warehouse_id, created_at DESC);


CREATE TABLE inventory_count_lines
(
    id              BIGSERIAL PRIMARY KEY,
    count_id        BIGINT         NOT NULL
        REFERENCES inventory_counts (id) ON DELETE CASCADE,
    tenant_id       BIGINT         NOT NULL REFERENCES tenants (id),
    product_id      BIGINT         NOT NULL,
    location_id     BIGINT         NOT NULL REFERENCES storage_locations (id),

    system_quantity NUMERIC(14, 3) NOT NULL,
    actual_quantity NUMERIC(14, 3) NOT NULL,

    CONSTRAINT fk_inventory_count_lines_product
        FOREIGN KEY (product_id, tenant_id)
            REFERENCES products (id, tenant_id),

    CONSTRAINT uq_inventory_count_line
        UNIQUE (count_id, product_id, location_id),

    CONSTRAINT chk_inventory_count_quantities
        CHECK (system_quantity >= 0 AND actual_quantity >= 0)
);

CREATE INDEX idx_inventory_count_lines_count
    ON inventory_count_lines (count_id);
