CREATE UNIQUE INDEX uq_shipment_line_reservation
    ON shipment_lines (reservation_id)
    WHERE reservation_id IS NOT NULL;