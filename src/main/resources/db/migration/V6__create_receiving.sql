CREATE TABLE receiving_documents
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    tenant_id       BIGINT      NOT NULL REFERENCES tenants (id),
    warehouse_id    BIGINT      NOT NULL,

    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    document_number VARCHAR(100),
    notes           TEXT,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    posted_at       TIMESTAMPTZ,

    CONSTRAINT fk_receiving_warehouse
        FOREIGN KEY (warehouse_id, tenant_id)
            REFERENCES warehouses (id, tenant_id),

    CONSTRAINT uq_receiving_document_number
        UNIQUE (tenant_id, document_number),

    CONSTRAINT chk_receiving_status
        CHECK (status IN ('DRAFT', 'POSTED', 'CANCELLED'))
);

CREATE INDEX idx_receiving_tenant_created
    ON receiving_documents (tenant_id, created_at DESC);

CREATE TABLE receiving_document_lines
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    document_id BIGINT         NOT NULL
        REFERENCES receiving_documents (id) ON DELETE CASCADE,

    tenant_id   BIGINT         NOT NULL,
    product_id  BIGINT         NOT NULL,
    location_id BIGINT         NOT NULL,

    quantity    NUMERIC(18, 3) NOT NULL,

    CONSTRAINT fk_receiving_line_product
        FOREIGN KEY (product_id, tenant_id)
            REFERENCES products (id, tenant_id),

    CONSTRAINT chk_receiving_line_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_receiving_lines_document
    ON receiving_document_lines (document_id);

CREATE INDEX idx_receiving_lines_product
    ON receiving_document_lines (tenant_id, product_id);