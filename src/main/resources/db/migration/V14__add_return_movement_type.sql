ALTER TABLE inventory_movements
    DROP CONSTRAINT chk_movement_type;

ALTER TABLE inventory_movements
    ADD CONSTRAINT chk_movement_type
        CHECK (
            movement_type IN (
                              'RECEIPT',
                              'TRANSFER_IN',
                              'TRANSFER_OUT',
                              'SHIPMENT',
                              'ADJUSTMENT',
                              'RETURN'
                )
            );