CREATE TABLE shipments
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    warehouse_id    BIGINT       NOT NULL,
    document_number VARCHAR(100) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    posted_at       TIMESTAMPTZ,

    CONSTRAINT fk_shipment_warehouse
        FOREIGN KEY (warehouse_id, tenant_id)
            REFERENCES warehouses (id, tenant_id),

    CONSTRAINT uq_shipment_document_number
        UNIQUE (tenant_id, document_number),

    CONSTRAINT chk_shipment_status
        CHECK (status IN ('DRAFT', 'POSTED', 'CANCELLED'))
);

CREATE INDEX idx_shipments_tenant_created
    ON shipments (tenant_id, created_at DESC);


CREATE TABLE shipment_lines
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    document_id BIGINT         NOT NULL
        REFERENCES shipments (id) ON DELETE CASCADE,
    tenant_id   BIGINT         NOT NULL,
    product_id  BIGINT         NOT NULL,
    location_id BIGINT         NOT NULL,
    quantity    NUMERIC(18, 3) NOT NULL,

    CONSTRAINT fk_shipment_line_product
        FOREIGN KEY (product_id, tenant_id)
            REFERENCES products (id, tenant_id),

    CONSTRAINT fk_shipment_line_location
        FOREIGN KEY (location_id)
            REFERENCES storage_locations (id),

    CONSTRAINT chk_shipment_line_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_shipment_lines_document
    ON shipment_lines (document_id);

CREATE INDEX idx_shipment_lines_product
    ON shipment_lines (tenant_id, product_id);
