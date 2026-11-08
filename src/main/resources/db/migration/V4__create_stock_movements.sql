-- material_id/service_order_id sem FK (mesma razao das migrations
-- anteriores -- ver V2/V3).
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id UUID,
    service_order_id UUID,
    movement_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_movements_service_order_id ON stock_movements(service_order_id);
CREATE INDEX idx_stock_movements_material_created ON stock_movements(material_id, created_at);
CREATE INDEX idx_stock_movements_order_type ON stock_movements(service_order_id, movement_type);
