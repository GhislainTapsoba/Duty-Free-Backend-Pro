-- V4__add_triggers.sql

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_suppliers_updated_at BEFORE UPDATE ON suppliers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_stocks_updated_at BEFORE UPDATE ON stocks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sommiers_updated_at BEFORE UPDATE ON sommiers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_purchase_orders_updated_at BEFORE UPDATE ON purchase_orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sales_updated_at BEFORE UPDATE ON sales
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_loyalty_cards_updated_at BEFORE UPDATE ON loyalty_cards
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_promotions_updated_at BEFORE UPDATE ON promotions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cash_registers_updated_at BEFORE UPDATE ON cash_registers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger to automatically update stock available quantity
CREATE OR REPLACE FUNCTION update_stock_available_quantity()
RETURNS TRIGGER AS $$
BEGIN
    NEW.available_quantity = NEW.quantity - NEW.reserved_quantity;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_stock_available BEFORE INSERT OR UPDATE ON stocks
    FOR EACH ROW EXECUTE FUNCTION update_stock_available_quantity();

-- Trigger to validate sommier value updates
CREATE OR REPLACE FUNCTION validate_sommier_update()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.cleared_value > NEW.initial_value THEN
        RAISE EXCEPTION 'Cleared value cannot exceed initial value';
    END IF;
    
    NEW.current_value = NEW.initial_value - NEW.cleared_value;
    
    -- Auto-update status
    IF NEW.cleared_value = NEW.initial_value THEN
        NEW.status = 'CLEARED';
        IF NEW.closing_date IS NULL THEN
            NEW.closing_date = CURRENT_DATE;
        END IF;
    ELSIF NEW.cleared_value > 0 THEN
        NEW.status = 'PARTIALLY_CLEARED';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_sommier BEFORE UPDATE ON sommiers
    FOR EACH ROW EXECUTE FUNCTION validate_sommier_update();

COMMENT ON FUNCTION update_updated_at_column() IS 'Automatically updates the updated_at timestamp';
COMMENT ON FUNCTION update_stock_available_quantity() IS 'Automatically calculates available stock quantity';
COMMENT ON FUNCTION validate_sommier_update() IS 'Validates and auto-updates sommier values and status';