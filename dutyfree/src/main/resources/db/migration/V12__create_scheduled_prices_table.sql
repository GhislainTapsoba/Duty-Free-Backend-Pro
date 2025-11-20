-- Migration V12: Create scheduled_prices table for dynamic pricing

CREATE TABLE IF NOT EXISTS scheduled_prices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    product_id BIGINT NOT NULL,
    price_type VARCHAR(20) NOT NULL DEFAULT 'FIXED',
    amount DECIMAL(19, 2) NOT NULL,
    percentage DECIMAL(5, 2),
    currency VARCHAR(3) DEFAULT 'XOF',
    valid_from DATE,
    valid_until DATE,
    time_from TIME,
    time_until TIME,
    days_of_week VARCHAR(100),
    priority INTEGER NOT NULL DEFAULT 0,
    period_type VARCHAR(20) DEFAULT 'PROMOTIONAL',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_scheduled_price_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_scheduled_price_product ON scheduled_prices(product_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_price_dates ON scheduled_prices(valid_from, valid_until);
CREATE INDEX IF NOT EXISTS idx_scheduled_price_active ON scheduled_prices(active);
CREATE INDEX IF NOT EXISTS idx_scheduled_price_deleted ON scheduled_prices(deleted);
CREATE INDEX IF NOT EXISTS idx_scheduled_price_period ON scheduled_prices(period_type);
CREATE INDEX IF NOT EXISTS idx_scheduled_price_priority ON scheduled_prices(priority DESC);

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_scheduled_prices_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_scheduled_prices_updated_at
    BEFORE UPDATE ON scheduled_prices
    FOR EACH ROW
    EXECUTE FUNCTION update_scheduled_prices_updated_at();

-- Comments
COMMENT ON TABLE scheduled_prices IS 'Prix programmés et dynamiques (promotions, happy hour, prix saisonniers)';
COMMENT ON COLUMN scheduled_prices.price_type IS 'Type de prix: FIXED (prix fixe), DISCOUNT (réduction), MARKUP (majoration)';
COMMENT ON COLUMN scheduled_prices.amount IS 'Nouveau prix (si FIXED) ou montant de réduction/majoration';
COMMENT ON COLUMN scheduled_prices.percentage IS 'Pourcentage de réduction/majoration (alternatif à amount)';
COMMENT ON COLUMN scheduled_prices.valid_from IS 'Date de début de validité';
COMMENT ON COLUMN scheduled_prices.valid_until IS 'Date de fin de validité';
COMMENT ON COLUMN scheduled_prices.time_from IS 'Heure de début (pour prix horaires)';
COMMENT ON COLUMN scheduled_prices.time_until IS 'Heure de fin (pour prix horaires)';
COMMENT ON COLUMN scheduled_prices.days_of_week IS 'Jours de la semaine applicables (MONDAY,TUESDAY,...)';
COMMENT ON COLUMN scheduled_prices.priority IS 'Priorité (si plusieurs règles s''appliquent)';
COMMENT ON COLUMN scheduled_prices.period_type IS 'Type de période: DAILY, WEEKLY, SEASONAL, PROMOTIONAL, SPECIAL_EVENT';
