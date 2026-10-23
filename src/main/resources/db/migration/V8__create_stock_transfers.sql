CREATE TABLE stock_transfers
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    tenant_id       BIGINT       NOT NULL REFERENCES tenants (id),
    warehouse_id    BIGINT       NOT NULL,

    document_number VARCHAR(100) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    notes           TEXT,

    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    posted_at       TIMESTAMPTZ,

    CONSTRAINT fk_stock_transfer_warehouse
        FOREIGN KEY (warehouse_id, tenant_id)
            REFERENCES warehouses (id, tenant_id),

    CONSTRAINT uq_stock_transfer_document_number
        UNIQUE (tenant_id, document_number),

    CONSTRAINT chk_stock_transfer_status
        CHECK (status IN ('DRAFT', 'POSTED', 'CANCELLED'))
);

CREATE INDEX idx_stock_transfers_tenant_created
    ON stock_transfers (tenant_id, created_at DESC);

CREATE TABLE stock_transfer_lines
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    document_id        BIGINT         NOT NULL
        REFERENCES stock_transfers (id) ON DELETE CASCADE,

    tenant_id          BIGINT         NOT NULL,
    product_id         BIGINT         NOT NULL,

    source_location_id BIGINT         NOT NULL,
    target_location_id BIGINT         NOT NULL,

    quantity           NUMERIC(18, 3) NOT NULL,

    CONSTRAINT fk_transfer_line_product
        FOREIGN KEY (product_id, tenant_id)
            REFERENCES products (id, tenant_id),

    CONSTRAINT fk_transfer_line_source_location
        FOREIGN KEY (source_location_id)
            REFERENCES storage_locations (id),

    CONSTRAINT fk_transfer_line_target_location
        FOREIGN KEY (target_location_id)
            REFERENCES storage_locations (id),

    CONSTRAINT chk_stock_transfer_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_stock_transfer_different_locations
        CHECK (source_location_id <> target_location_id)
);

CREATE INDEX idx_stock_transfer_lines_document
    ON stock_transfer_lines (document_id);

CREATE INDEX idx_stock_transfer_lines_product
    ON stock_transfer_lines (tenant_id, product_id);