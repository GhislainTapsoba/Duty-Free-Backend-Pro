-- V3__add_indexes.sql

-- Créer l'extension pg_trgm si elle n'existe pas
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Performance indexes for commonly queried fields
-- Users indexes
CREATE INDEX idx_users_role ON users(role) WHERE deleted = FALSE;
-- ... (le reste du fichier)
CREATE INDEX idx_users_active ON users(active) WHERE deleted = FALSE;
CREATE INDEX idx_users_username_active ON users(username, active) WHERE deleted = FALSE;

-- Categories indexes
CREATE INDEX idx_categories_parent ON categories(parent_id) WHERE deleted = FALSE;
CREATE INDEX idx_categories_active ON categories(active) WHERE deleted = FALSE;
CREATE INDEX idx_categories_code_active ON categories(code, active) WHERE deleted = FALSE;

-- Products indexes
CREATE INDEX idx_products_category ON products(category_id) WHERE deleted = FALSE;
CREATE INDEX idx_products_supplier ON products(supplier_id) WHERE deleted = FALSE;
CREATE INDEX idx_products_active ON products(active) WHERE deleted = FALSE;
CREATE INDEX idx_products_name_fr ON products(name_fr) WHERE deleted = FALSE;
CREATE INDEX idx_products_name_en ON products(name_en) WHERE deleted = FALSE;
CREATE INDEX idx_products_track_stock ON products(track_stock) WHERE deleted = FALSE AND active = TRUE;

-- Full text search index for products
CREATE INDEX idx_products_name_fr_trgm ON products USING gin(name_fr gin_trgm_ops);
CREATE INDEX idx_products_name_en_trgm ON products USING gin(name_en gin_trgm_ops);

-- Stocks indexes
CREATE INDEX idx_stocks_sommier ON stocks(sommier_id) WHERE deleted = FALSE;
CREATE INDEX idx_stocks_available ON stocks(available_quantity) WHERE deleted = FALSE;
CREATE INDEX idx_stocks_expiry ON stocks(expiry_date) WHERE deleted = FALSE AND expiry_date IS NOT NULL;

-- Sommiers indexes
CREATE INDEX idx_sommiers_status ON sommiers(status) WHERE deleted = FALSE;
CREATE INDEX idx_sommiers_opening_date ON sommiers(opening_date) WHERE deleted = FALSE;
CREATE INDEX idx_sommiers_alert ON sommiers(alert_date, alert_sent) WHERE deleted = FALSE AND status = 'ACTIVE';
CREATE INDEX idx_sommiers_purchase_order ON sommiers(purchase_order_id);

-- Purchase Orders indexes
CREATE INDEX idx_purchase_orders_supplier ON purchase_orders(supplier_id) WHERE deleted = FALSE;
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status) WHERE deleted = FALSE;
CREATE INDEX idx_purchase_orders_order_date ON purchase_orders(order_date) WHERE deleted = FALSE;
CREATE INDEX idx_purchase_orders_expected_delivery ON purchase_orders(expected_delivery_date) 
    WHERE deleted = FALSE AND status NOT IN ('RECEIVED', 'CANCELLED');

-- Sales indexes
CREATE INDEX idx_sales_cashier ON sales(cashier_id) WHERE deleted = FALSE;
CREATE INDEX idx_sales_customer ON sales(customer_id) WHERE deleted = FALSE;
CREATE INDEX idx_sales_cash_register ON sales(cash_register_id) WHERE deleted = FALSE;
CREATE INDEX idx_sales_status ON sales(status) WHERE deleted = FALSE;
CREATE INDEX idx_sales_date_status ON sales(sale_date, status) WHERE deleted = FALSE;
CREATE INDEX idx_sales_date_register ON sales(sale_date, cash_register_id) WHERE deleted = FALSE;

-- Sale Items indexes
CREATE INDEX idx_sale_items_sale ON sale_items(sale_id) WHERE deleted = FALSE;
CREATE INDEX idx_sale_items_product ON sale_items(product_id) WHERE deleted = FALSE;
CREATE INDEX idx_sale_items_promotion ON sale_items(promotion_id) WHERE deleted = FALSE;

-- Payments indexes
CREATE INDEX idx_payments_sale ON payments(sale_id) WHERE deleted = FALSE;
CREATE INDEX idx_payments_method ON payments(payment_method) WHERE deleted = FALSE;
CREATE INDEX idx_payments_date ON payments(payment_date) WHERE deleted = FALSE;
CREATE INDEX idx_payments_verified ON payments(verified) WHERE deleted = FALSE;
CREATE INDEX idx_payments_currency ON payments(currency) WHERE deleted = FALSE;

-- Customers indexes
CREATE INDEX idx_customers_active ON customers(active) WHERE deleted = FALSE;
CREATE INDEX idx_customers_vip ON customers(is_vip) WHERE deleted = FALSE AND active = TRUE;
CREATE INDEX idx_customers_name ON customers(first_name, last_name) WHERE deleted = FALSE;

-- Loyalty Cards indexes
CREATE INDEX idx_loyalty_cards_active ON loyalty_cards(active) WHERE deleted = FALSE;
CREATE INDEX idx_loyalty_cards_tier ON loyalty_cards(tier) WHERE deleted = FALSE AND active = TRUE;
CREATE INDEX idx_loyalty_cards_expiry ON loyalty_cards(expiry_date) WHERE deleted = FALSE AND active = TRUE;

-- Promotions indexes
CREATE INDEX idx_promotions_active ON promotions(active) WHERE deleted = FALSE;
CREATE INDEX idx_promotions_dates ON promotions(start_date, end_date) WHERE deleted = FALSE AND active = TRUE;
CREATE INDEX idx_promotions_code_active ON promotions(code, active) WHERE deleted = FALSE;

-- Cash Registers indexes
CREATE INDEX idx_cash_registers_active ON cash_registers(active) WHERE deleted = FALSE;
CREATE INDEX idx_cash_registers_open ON cash_registers(is_open) WHERE deleted = FALSE AND active = TRUE;
CREATE INDEX idx_cash_registers_location ON cash_registers(location) WHERE deleted = FALSE AND active = TRUE;

-- Receipts indexes
CREATE INDEX idx_receipts_printed ON receipts(printed) WHERE deleted = FALSE;
CREATE INDEX idx_receipts_emailed ON receipts(emailed) WHERE deleted = FALSE;
CREATE INDEX idx_receipts_printed_date ON receipts(printed_date) WHERE deleted = FALSE;

-- Exchange Rates indexes
CREATE INDEX idx_exchange_rates_active ON exchange_rates(active) WHERE deleted = FALSE;

-- Composite indexes for common queries
CREATE INDEX idx_sales_cashier_date_status ON sales(cashier_id, sale_date, status) WHERE deleted = FALSE;
CREATE INDEX idx_sales_register_date_status ON sales(cash_register_id, sale_date, status) WHERE deleted = FALSE;
CREATE INDEX idx_products_category_active ON products(category_id, active) WHERE deleted = FALSE;
CREATE INDEX idx_stocks_product_available ON stocks(product_id, available_quantity) WHERE deleted = FALSE;

-- Partial indexes for better query performance
CREATE INDEX idx_sales_completed ON sales(sale_date) WHERE status = 'COMPLETED' AND deleted = FALSE;
CREATE INDEX idx_sales_pending ON sales(sale_date) WHERE status = 'PENDING' AND deleted = FALSE;
CREATE INDEX idx_products_low_stock ON products(id) 
    WHERE track_stock = TRUE AND active = TRUE AND deleted = FALSE;

-- Statistics update
ANALYZE users;
ANALYZE categories;
ANALYZE products;
ANALYZE stocks;
ANALYZE sommiers;
ANALYZE sales;
ANALYZE sale_items;
ANALYZE payments;
ANALYZE customers;
ANALYZE cash_registers;

-- Comments for documentation
COMMENT ON INDEX idx_sales_completed IS 'Optimizes queries for completed sales reports';
COMMENT ON INDEX idx_sales_pending IS 'Optimizes queries for pending sales';
COMMENT ON INDEX idx_products_low_stock IS 'Optimizes low stock alert queries';
COMMENT ON INDEX idx_stocks_product_available IS 'Optimizes available stock queries per product';