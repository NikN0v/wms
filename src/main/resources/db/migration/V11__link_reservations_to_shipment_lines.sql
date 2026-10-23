ALTER TABLE shipment_lines
    ADD COLUMN reservation_id BIGINT
        REFERENCES stock_reservations (id);

CREATE INDEX idx_shipment_lines_reservation
    ON shipment_lines (reservation_id);
