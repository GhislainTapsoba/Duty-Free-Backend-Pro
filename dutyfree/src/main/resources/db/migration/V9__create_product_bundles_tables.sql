-- Migration V9: Create product_bundles and bundle_items tables
-- Purpose: Support menu formulas and product bundles (breakfast menus, combos, etc.)

-- Create product_bundles table
CREATE TABLE IF NOT EXISTS product_bundles (
    id BIGSERIAL PRIMARY KEY,
    bundle_code VARCHAR(50) NOT NULL UNIQUE,
    name_fr VARCHAR(200) NOT NULL,
    name_en VARCHAR(200) NOT NULL,
    description_fr VARCHAR(1000),
    description_en VARCHAR(1000),
    category_id BIGINT,
    bundle_price_xof DECIMAL(19, 2),
    bundle_price_eur DECIMAL(19, 2),
    bundle_price_usd DECIMAL(19, 2),
    discount_percentage DECIMAL(5, 2) DEFAULT 0,
    bundle_type VARCHAR(50) DEFAULT 'MENU',
    image_url VARCHAR(500),
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    time_restricted BOOLEAN NOT NULL DEFAULT FALSE,
    start_time VARCHAR(5),
    end_time VARCHAR(5),
    daily_limit INTEGER,
    today_sold_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_product_bundles_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Create bundle_items table
CREATE TABLE IF NOT EXISTS bundle_items (
    id BIGSERIAL PRIMARY KEY,
    bundle_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    optional BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    notes VARCHAR(500),
    substitutable BOOLEAN DEFAULT FALSE,
    substitution_group VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_bundle_items_bundle FOREIGN KEY (bundle_id) REFERENCES product_bundles(id) ON DELETE CASCADE,
    CONSTRAINT fk_bundle_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create indexes for better performance
CREATE INDEX idx_product_bundles_code ON product_bundles(bundle_code);
CREATE INDEX idx_product_bundles_active ON product_bundles(active);
CREATE INDEX idx_product_bundles_type ON product_bundles(bundle_type);
CREATE INDEX idx_product_bundles_category ON product_bundles(category_id);
CREATE INDEX idx_product_bundles_valid_from ON product_bundles(valid_from);
CREATE INDEX idx_product_bundles_valid_until ON product_bundles(valid_until);
CREATE INDEX idx_product_bundles_deleted ON product_bundles(deleted) WHERE deleted = FALSE;

CREATE INDEX idx_bundle_items_bundle ON bundle_items(bundle_id);
CREATE INDEX idx_bundle_items_product ON bundle_items(product_id);
CREATE INDEX idx_bundle_items_order ON bundle_items(bundle_id, display_order);
CREATE INDEX idx_bundle_items_deleted ON bundle_items(deleted) WHERE deleted = FALSE;

-- Trigger to update updated_at timestamp for product_bundles
CREATE OR REPLACE FUNCTION update_product_bundles_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_product_bundles_updated_at
    BEFORE UPDATE ON product_bundles
    FOR EACH ROW
    EXECUTE FUNCTION update_product_bundles_updated_at();

-- Trigger to update updated_at timestamp for bundle_items
CREATE OR REPLACE FUNCTION update_bundle_items_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_bundle_items_updated_at
    BEFORE UPDATE ON bundle_items
    FOR EACH ROW
    EXECUTE FUNCTION update_bundle_items_updated_at();

-- Comments on tables
COMMENT ON TABLE product_bundles IS 'Product bundles and menu formulas (e.g., breakfast menus, combos)';
COMMENT ON COLUMN product_bundles.bundle_type IS 'Type of bundle: MENU (meal), COMBO (grouped products), FORMULA (special formula)';
COMMENT ON COLUMN product_bundles.time_restricted IS 'Whether bundle is only available at certain hours';
COMMENT ON COLUMN product_bundles.daily_limit IS 'Maximum number of sales per day (for limited promotions)';
COMMENT ON COLUMN product_bundles.today_sold_count IS 'Counter for today sales (reset daily)';

COMMENT ON TABLE bundle_items IS 'Products that compose a bundle';
COMMENT ON COLUMN bundle_items.optional IS 'Whether the product is optional in the bundle';
COMMENT ON COLUMN bundle_items.substitutable IS 'Whether the product can be substituted with another';
COMMENT ON COLUMN bundle_items.substitution_group IS 'Group of products that can substitute each other (e.g., HOT_BEVERAGE)';

-- Insert some example bundles (optional - can be removed in production)
-- Example 1: Breakfast Menu
INSERT INTO product_bundles (
    bundle_code, name_fr, name_en, description_fr, description_en,
    bundle_type, discount_percentage, time_restricted, start_time, end_time,
    active, created_at
) VALUES (
    'BFAST001',
    'Petit Déjeuner Complet',
    'Complete Breakfast',
    'Café ou thé + croissant + jus d''orange',
    'Coffee or tea + croissant + orange juice',
    'MENU',
    10.00,
    TRUE,
    '06:00',
    '11:00',
    TRUE,
    CURRENT_TIMESTAMP
) ON CONFLICT (bundle_code) DO NOTHING;
