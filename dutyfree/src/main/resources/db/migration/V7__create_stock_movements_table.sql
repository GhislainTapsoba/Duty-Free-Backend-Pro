-- Migration V7: Create stock_movements table
-- Purpose: Track all stock movements (entries, sales, adjustments, transfers)

CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    stock_id BIGINT,
    sommier_id BIGINT,
    movement_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    reference VARCHAR(100),
    movement_date TIMESTAMP,
    notes TEXT,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    -- Foreign keys
    CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_stock_movements_stock FOREIGN KEY (stock_id) REFERENCES stocks(id),
    CONSTRAINT fk_stock_movements_sommier FOREIGN KEY (sommier_id) REFERENCES sommiers(id)
);

-- Indexes for better performance
CREATE INDEX idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_stock_id ON stock_movements(stock_id);
CREATE INDEX idx_stock_movements_type ON stock_movements(movement_type);
CREATE INDEX idx_stock_movements_date ON stock_movements(movement_date);
CREATE INDEX idx_stock_movements_deleted ON stock_movements(deleted) WHERE deleted = false;

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_stock_movements_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_stock_movements_updated_at
    BEFORE UPDATE ON stock_movements
    FOR EACH ROW
    EXECUTE FUNCTION update_stock_movements_updated_at();

-- Comment on table
COMMENT ON TABLE stock_movements IS 'Tracks all stock movements including entries, sales, adjustments, and transfers';
COMMENT ON COLUMN stock_movements.movement_type IS 'Type of movement: ENTRY, SALE, ADJUSTMENT, TRANSFER, RETURN';
COMMENT ON COLUMN stock_movements.quantity IS 'Quantity moved (positive for entries, negative for sales)';
COMMENT ON COLUMN stock_movements.reference IS 'Reference number (sale number, purchase order number, etc.)';
