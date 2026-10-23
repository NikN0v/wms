CREATE TABLE inventory_movements
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    tenant_id             BIGINT         NOT NULL REFERENCES tenants (id),
    product_id            BIGINT         NOT NULL,
    location_id           BIGINT         NOT NULL,

    movement_type         VARCHAR(30)    NOT NULL,
    quantity              NUMERIC(18, 3) NOT NULL,

    receiving_document_id BIGINT
        REFERENCES receiving_documents (id),
    receiving_line_id     BIGINT
        REFERENCES receiving_document_lines (id),

    created_at            TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movement_product
        FOREIGN KEY (product_id, tenant_id)
            REFERENCES products (id, tenant_id),

    CONSTRAINT chk_movement_type
        CHECK (
            movement_type IN (
                              'RECEIPT',
                              'TRANSFER_IN',
                              'TRANSFER_OUT',
                              'SHIPMENT',
                              'ADJUSTMENT'
                )
            ),

    CONSTRAINT chk_movement_quantity
        CHECK (quantity > 0),

    CONSTRAINT uq_movement_receiving_line
        UNIQUE (receiving_line_id)
);

CREATE INDEX idx_movements_tenant_created
    ON inventory_movements (tenant_id, created_at DESC);

CREATE INDEX idx_movements_product
    ON inventory_movements (tenant_id, product_id, created_at DESC);

CREATE INDEX idx_movements_location
    ON inventory_movements (tenant_id, location_id, created_at DESC);