CREATE TABLE inventory
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    tenant_id         BIGINT         NOT NULL REFERENCES tenants (id),
    product_id        BIGINT         NOT NULL REFERENCES products (id),
    location_id       BIGINT         NOT NULL REFERENCES storage_locations (id),

    quantity          NUMERIC(18, 3) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(18, 3) NOT NULL DEFAULT 0,

    created_at        TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_inventory_product_location
        UNIQUE (tenant_id, product_id, location_id),

    CONSTRAINT chk_inventory_quantity
        CHECK (quantity >= 0),

    CONSTRAINT chk_inventory_reserved
        CHECK (
            reserved_quantity >= 0
                AND reserved_quantity <= quantity
            )
);

CREATE INDEX idx_inventory_tenant
    ON inventory (tenant_id);

CREATE INDEX idx_inventory_product
    ON inventory (tenant_id, product_id);

CREATE INDEX idx_inventory_location
    ON inventory (tenant_id, location_id);