CREATE TABLE stock_reservations
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    tenant_id      BIGINT         NOT NULL,
    product_id     BIGINT         NOT NULL,
    location_id    BIGINT         NOT NULL,

    quantity       NUMERIC(18, 3) NOT NULL,

    status         VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',

    reference_type VARCHAR(50),
    reference_id   VARCHAR(100),

    created_at     TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at    TIMESTAMPTZ,

    CONSTRAINT fk_reservation_product
        FOREIGN KEY (product_id, tenant_id)
            REFERENCES products (id, tenant_id),

    CONSTRAINT fk_reservation_location
        FOREIGN KEY (location_id)
            REFERENCES storage_locations (id),

    CONSTRAINT chk_reservation_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_reservation_status
        CHECK (status IN ('ACTIVE', 'RELEASED', 'CONSUMED'))
);

CREATE INDEX idx_stock_reservations_tenant_status
    ON stock_reservations (tenant_id, status);

CREATE INDEX idx_stock_reservations_product_location
    ON stock_reservations (tenant_id, product_id, location_id);

CREATE INDEX idx_stock_reservations_reference
    ON stock_reservations (reference_type, reference_id);
