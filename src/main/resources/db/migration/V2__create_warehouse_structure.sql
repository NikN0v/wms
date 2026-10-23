CREATE TABLE tenants (
                         id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         name VARCHAR(150) NOT NULL,
                         code VARCHAR(50) NOT NULL UNIQUE,
                         is_active BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE warehouses (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            tenant_id BIGINT NOT NULL,
                            name VARCHAR(150) NOT NULL,
                            code VARCHAR(50) NOT NULL,
                            address VARCHAR(500),
                            is_active BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_warehouses_tenant
                                FOREIGN KEY (tenant_id)
                                    REFERENCES tenants (id),

                            CONSTRAINT uq_warehouses_tenant_code
                                UNIQUE (tenant_id, code),

                            CONSTRAINT uq_warehouses_id_tenant
                                UNIQUE (id, tenant_id)
);

CREATE TABLE storage_zones (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               tenant_id BIGINT NOT NULL,
                               warehouse_id BIGINT NOT NULL,
                               name VARCHAR(150) NOT NULL,
                               code VARCHAR(50) NOT NULL,
                               is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_zones_warehouse_tenant
                                   FOREIGN KEY (warehouse_id, tenant_id)
                                       REFERENCES warehouses (id, tenant_id),

                               CONSTRAINT uq_zones_warehouse_code
                                   UNIQUE (warehouse_id, code),

                               CONSTRAINT uq_zones_id_tenant
                                   UNIQUE (id, tenant_id)
);

CREATE TABLE storage_locations (
                                   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   tenant_id BIGINT NOT NULL,
                                   zone_id BIGINT NOT NULL,
                                   code VARCHAR(50) NOT NULL,
                                   is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_locations_zone_tenant
                                       FOREIGN KEY (zone_id, tenant_id)
                                           REFERENCES storage_zones (id, tenant_id),

                                   CONSTRAINT uq_locations_zone_code
                                       UNIQUE (zone_id, code)
);

CREATE INDEX idx_warehouses_tenant_id
    ON warehouses (tenant_id);

CREATE INDEX idx_storage_zones_warehouse_id
    ON storage_zones (warehouse_id);

CREATE INDEX idx_storage_locations_zone_id
    ON storage_locations (zone_id);