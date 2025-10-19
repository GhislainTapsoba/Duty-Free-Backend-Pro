-- V1__init_schema.sql

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP,
    cash_register_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Categories table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    code VARCHAR(50) NOT NULL UNIQUE,
    parent_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);

-- Suppliers table
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) UNIQUE,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(50),
    tax_id VARCHAR(50),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name_fr VARCHAR(200) NOT NULL,
    name_en VARCHAR(200) NOT NULL,
    description_fr TEXT,
    description_en TEXT,
    barcode VARCHAR(50) UNIQUE,
    category_id BIGINT NOT NULL,
    supplier_id BIGINT,
    purchase_price DECIMAL(19,2) NOT NULL,
    selling_price_xof DECIMAL(19,2) NOT NULL,
    selling_price_eur DECIMAL(19,2),
    selling_price_usd DECIMAL(19,2),
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    track_stock BOOLEAN NOT NULL DEFAULT TRUE,
    min_stock_level INTEGER DEFAULT 0,
    reorder_level INTEGER DEFAULT 0,
    unit VARCHAR(50) DEFAULT 'PIECE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_sku ON products(sku);

-- Purchase Orders table
CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    order_date DATE NOT NULL,
    expected_delivery_date DATE,
    received_date DATE,
    status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(19,2) NOT NULL DEFAULT 0,
    transport_cost DECIMAL(19,2) NOT NULL DEFAULT 0,
    insurance_cost DECIMAL(19,2) NOT NULL DEFAULT 0,
    other_costs DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_cost DECIMAL(19,2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

-- Purchase Order Items table
CREATE TABLE purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity_ordered INTEGER NOT NULL,
    quantity_received INTEGER NOT NULL DEFAULT 0,
    unit_price DECIMAL(19,2) NOT NULL,
    total_price DECIMAL(19,2) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Sommiers table
CREATE TABLE sommiers (
    id BIGSERIAL PRIMARY KEY,
    sommier_number VARCHAR(50) NOT NULL UNIQUE,
    purchase_order_id BIGINT,
    opening_date DATE NOT NULL,
    closing_date DATE,
    initial_value DECIMAL(19,2) NOT NULL DEFAULT 0,
    current_value DECIMAL(19,2) NOT NULL DEFAULT 0,
    cleared_value DECIMAL(19,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    alert_date DATE,
    alert_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id)
);

-- Stocks table
CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sommier_id BIGINT,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    location VARCHAR(100),
    lot_number VARCHAR(50),
    expiry_date DATE,
    received_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (sommier_id) REFERENCES sommiers(id)
);

CREATE INDEX idx_stocks_product_location ON stocks(product_id, location);

-- Cash Registers table
CREATE TABLE cash_registers (
    id BIGSERIAL PRIMARY KEY,
    register_number VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    is_open BOOLEAN NOT NULL DEFAULT FALSE,
    opened_at TIMESTAMP,
    closed_at TIMESTAMP,
    opened_by_user_id BIGINT,
    closed_by_user_id BIGINT,
    opening_balance DECIMAL(19,2) DEFAULT 0,
    closing_balance DECIMAL(19,2) DEFAULT 0,
    expected_balance DECIMAL(19,2) DEFAULT 0,
    cash_in_drawer DECIMAL(19,2) DEFAULT 0,
    ip_address VARCHAR(100),
    terminal_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (opened_by_user_id) REFERENCES users(id),
    FOREIGN KEY (closed_by_user_id) REFERENCES users(id)
);

-- Add FK to users table
ALTER TABLE users ADD FOREIGN KEY (cash_register_id) REFERENCES cash_registers(id);

-- Customers table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    date_of_birth DATE,
    gender VARCHAR(20),
    nationality VARCHAR(50),
    passport_number VARCHAR(100),
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(50),
    postal_code VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    badge_number VARCHAR(100),
    company_name VARCHAR(100),
    is_vip BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_phone ON customers(phone);

-- Loyalty Cards table
CREATE TABLE loyalty_cards (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE,
    card_number VARCHAR(50) NOT NULL UNIQUE,
    points_balance DECIMAL(19,2) NOT NULL DEFAULT 0,
    wallet_balance DECIMAL(19,2) NOT NULL DEFAULT 0,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    tier VARCHAR(50) DEFAULT 'STANDARD',
    discount_percentage DECIMAL(5,2) DEFAULT 0,
    last_used_date DATE,
    total_purchases INTEGER NOT NULL DEFAULT 0,
    total_spent DECIMAL(19,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Promotions table
CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    discount_type VARCHAR(20) DEFAULT 'PERCENTAGE',
    discount_value DECIMAL(19,2) NOT NULL,
    minimum_purchase_amount DECIMAL(19,2),
    maximum_discount_amount DECIMAL(19,2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    stackable BOOLEAN NOT NULL DEFAULT FALSE,
    usage_limit INTEGER,
    usage_count INTEGER NOT NULL DEFAULT 0,
    apply_to_all_products BOOLEAN NOT NULL DEFAULT FALSE,
    terms TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Promotion Products junction table
CREATE TABLE promotion_products (
    promotion_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (promotion_id, product_id),
    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Promotion Categories junction table
CREATE TABLE promotion_categories (
    promotion_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (promotion_id, category_id),
    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Sales table
CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    sale_number VARCHAR(50) NOT NULL UNIQUE,
    sale_date TIMESTAMP NOT NULL,
    cashier_id BIGINT NOT NULL,
    customer_id BIGINT,
    cash_register_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(19,2) NOT NULL DEFAULT 0,
    discount DECIMAL(19,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    notes TEXT,
    passenger_name VARCHAR(100),
    flight_number VARCHAR(50),
    airline VARCHAR(100),
    destination VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (cashier_id) REFERENCES users(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (cash_register_id) REFERENCES cash_registers(id)
);

CREATE INDEX idx_sales_sale_date ON sales(sale_date);
CREATE INDEX idx_sales_sale_number ON sales(sale_number);

-- Sale Items table
CREATE TABLE sale_items (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    discount DECIMAL(19,2) NOT NULL DEFAULT 0,
    tax_rate DECIMAL(5,2) NOT NULL,
    tax_amount DECIMAL(19,2) NOT NULL,
    total_price DECIMAL(19,2) NOT NULL,
    promotion_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (promotion_id) REFERENCES promotions(id)
);

-- Payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount_in_currency DECIMAL(19,2) NOT NULL,
    amount_in_xof DECIMAL(19,2) NOT NULL,
    exchange_rate DECIMAL(10,6),
    payment_date TIMESTAMP NOT NULL,
    transaction_reference VARCHAR(100),
    card_last4_digits VARCHAR(100),
    card_type VARCHAR(50),
    mobile_money_provider VARCHAR(100),
    mobile_money_number VARCHAR(100),
    notes TEXT,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE
);

-- Receipts table
CREATE TABLE receipts (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL UNIQUE,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    printed_date TIMESTAMP NOT NULL,
    receipt_content TEXT,
    pdf_path VARCHAR(500),
    printed BOOLEAN NOT NULL DEFAULT FALSE,
    emailed BOOLEAN NOT NULL DEFAULT FALSE,
    email_address VARCHAR(100),
    emailed_date TIMESTAMP,
    header_message VARCHAR(500),
    footer_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (sale_id) REFERENCES sales(id)
);

-- Exchange Rates table
CREATE TABLE exchange_rates (
    id BIGSERIAL PRIMARY KEY,
    currency VARCHAR(10) NOT NULL,
    rate_to_xof DECIMAL(10,6) NOT NULL,
    effective_date DATE NOT NULL,
    expiry_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    source VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (currency, effective_date)
);

CREATE INDEX idx_exchange_rates_currency_date ON exchange_rates(currency, effective_date);