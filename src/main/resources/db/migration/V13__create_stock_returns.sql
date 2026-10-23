CREATE TABLE stock_returns
(
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL REFERENCES tenants (id),
    warehouse_id    BIGINT       NOT NULL REFERENCES warehouses (id),
    document_number VARCHAR(100) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    posted_at       TIMESTAMPTZ,

    CONSTRAINT chk_stock_returns_status
        CHECK (status IN ('DRAFT', 'POSTED')),

    CONSTRAINT uq_stock_returns_tenant_document
        UNIQUE (tenant_id, document_number)
);

CREATE INDEX idx_stock_returns_warehouse
    ON stock_returns (tenant_id, warehouse_id, created_at DESC);


CREATE TABLE stock_return_lines
(
    id          BIGSERIAL PRIMARY KEY,
    return_id   BIGINT         NOT NULL
        REFERENCES stock_returns (id) ON DELETE CASCADE,
    tenant_id   BIGINT         NOT NULL REFERENCES tenants (id),
    product_id  BIGINT         NOT NULL,
    location_id BIGINT         NOT NULL REFERENCES storage_locations (id),
    quantity    NUMERIC(14, 3) NOT NULL,

    CONSTRAINT fk_stock_return_lines_product
        FOREIGN KEY (product_id, tenant_id)
            REFERENCES products (id, tenant_id),

    CONSTRAINT chk_stock_return_lines_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_stock_return_lines_return
    ON stock_return_lines (return_id);

CREATE INDEX idx_stock_return_lines_product
    ON stock_return_lines (tenant_id, product_id);
