ALTER TABLE storage_locations
    ADD COLUMN type VARCHAR(30) NOT NULL DEFAULT 'STANDARD';

ALTER TABLE storage_locations
    ADD CONSTRAINT chk_storage_locations_type
    CHECK (type IN ('STANDARD', 'RECEIVING', 'SHIPPING', 'PICKING'));