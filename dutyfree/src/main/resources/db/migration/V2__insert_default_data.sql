-- V2__insert_default_data.sql

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, full_name, email, phone, role, active, created_by)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
        'System Administrator', 'admin@dutyfree.com', '+22670000000', 'ADMIN', TRUE, 'SYSTEM');

-- Insert default superviseur user (password: super123)
INSERT INTO users (username, password, full_name, email, phone, role, active, created_by)
VALUES ('superviseur', '$2a$10$rPxJZ8w7mZ9YvWq6nQH7.ewRXQFH8fW5XvW7jKnJKnHqKnJKnJKn', 
        'Superviseur Principal', 'superviseur@dutyfree.com', '+22670000001', 'SUPERVISEUR', TRUE, 'SYSTEM');

-- Insert default caissier user (password: caisse123)
INSERT INTO users (username, password, full_name, email, phone, role, active, created_by)
VALUES ('caissier', '$2a$10$8Z8mZ8mZ8mZ8mZ8mZ8mZ8.Z8mZ8mZ8mZ8mZ8mZ8mZ8mZ8mZ8mZ', 
        'Caissier 1', 'caissier@dutyfree.com', '+22670000002', 'CAISSIER', TRUE, 'SYSTEM');

-- Insert default stock manager (password: stock123)
INSERT INTO users (username, password, full_name, email, phone, role, active, created_by)
VALUES ('stockmgr', '$2a$10$7Y7lY7lY7lY7lY7lY7lY7.Y7lY7lY7lY7lY7lY7lY7lY7lY7lY', 
        'Stock Manager', 'stock@dutyfree.com', '+22670000003', 'STOCK_MANAGER', TRUE, 'SYSTEM');

-- Insert default categories
INSERT INTO categories (name, description, code, active, created_by) VALUES
('Alcools', 'Boissons alcoolisées', 'ALC', TRUE, 'SYSTEM'),
('Parfums', 'Parfums et eaux de toilette', 'PARF', TRUE, 'SYSTEM'),
('Cosmétiques', 'Produits de beauté et cosmétiques', 'COSM', TRUE, 'SYSTEM'),
('Tabac', 'Cigarettes et produits du tabac', 'TAB', TRUE, 'SYSTEM'),
('Confiserie', 'Chocolats et confiseries', 'CONF', TRUE, 'SYSTEM'),
('Maroquinerie', 'Sacs, valises et accessoires', 'MARO', TRUE, 'SYSTEM'),
('Montres', 'Montres et horlogerie', 'MONT', TRUE, 'SYSTEM'),
('Bijouterie', 'Bijoux et accessoires', 'BIJ', TRUE, 'SYSTEM'),
('Électronique', 'Appareils électroniques', 'ELEC', TRUE, 'SYSTEM'),
('Mode', 'Vêtements et accessoires de mode', 'MODE', TRUE, 'SYSTEM');

-- Insert sub-categories for Alcools
INSERT INTO categories (name, description, code, parent_id, active, created_by) 
SELECT 'Whisky', 'Whisky et bourbon', 'ALC-WHI', id, TRUE, 'SYSTEM' FROM categories WHERE code = 'ALC';

INSERT INTO categories (name, description, code, parent_id, active, created_by) 
SELECT 'Vin', 'Vins rouges, blancs et rosés', 'ALC-VIN', id, TRUE, 'SYSTEM' FROM categories WHERE code = 'ALC';

INSERT INTO categories (name, description, code, parent_id, active, created_by) 
SELECT 'Champagne', 'Champagnes et vins pétillants', 'ALC-CHA', id, TRUE, 'SYSTEM' FROM categories WHERE code = 'ALC';

INSERT INTO categories (name, description, code, parent_id, active, created_by) 
SELECT 'Vodka', 'Vodka premium et standard', 'ALC-VOD', id, TRUE, 'SYSTEM' FROM categories WHERE code = 'ALC';

INSERT INTO categories (name, description, code, parent_id, active, created_by) 
SELECT 'Cognac', 'Cognac et brandy', 'ALC-COG', id, TRUE, 'SYSTEM' FROM categories WHERE code = 'ALC';

-- Insert default suppliers
INSERT INTO suppliers (name, code, contact_person, email, phone, city, country, active, created_by) VALUES
('Moët Hennessy', 'SUP-MH', 'Jean Dupont', 'contact@moethennessy.com', '+33142345678', 'Paris', 'France', TRUE, 'SYSTEM'),
('L''Oréal Luxe', 'SUP-LOR', 'Marie Martin', 'contact@loreal.com', '+33144567890', 'Paris', 'France', TRUE, 'SYSTEM'),
('LVMH Parfums', 'SUP-LVMH', 'Pierre Dubois', 'contact@lvmh.com', '+33145678901', 'Paris', 'France', TRUE, 'SYSTEM'),
('Diageo', 'SUP-DIA', 'John Smith', 'contact@diageo.com', '+442071234567', 'London', 'UK', TRUE, 'SYSTEM'),
('Pernod Ricard', 'SUP-PR', 'François Laurent', 'contact@pernod-ricard.com', '+33141234567', 'Paris', 'France', TRUE, 'SYSTEM');

-- Insert default cash registers
INSERT INTO cash_registers (register_number, name, location, active, created_by) VALUES
('CR-001', 'Caisse Principale 1', 'Zone Départs - Terminal 1', TRUE, 'SYSTEM'),
('CR-002', 'Caisse Principale 2', 'Zone Départs - Terminal 1', TRUE, 'SYSTEM'),
('CR-003', 'Caisse Secondaire 1', 'Zone Départs - Terminal 2', TRUE, 'SYSTEM');

-- Insert default exchange rates
INSERT INTO exchange_rates (currency, rate_to_xof, effective_date, active, source, created_by) VALUES
('EUR', 655.957, CURRENT_DATE, TRUE, 'Banque Centrale', 'SYSTEM'),
('USD', 600.00, CURRENT_DATE, TRUE, 'Banque Centrale', 'SYSTEM');

-- Insert sample products (Whisky)
INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'WHI-JW-RED', 'Johnnie Walker Red Label 70cl', 'Johnnie Walker Red Label 70cl',
       c.id, s.id, 8000, 15000, 23, 25, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'ALC-WHI' AND s.code = 'SUP-DIA';

INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'WHI-JW-BLK', 'Johnnie Walker Black Label 70cl', 'Johnnie Walker Black Label 70cl',
       c.id, s.id, 12000, 22000, 34, 37, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'ALC-WHI' AND s.code = 'SUP-DIA';

INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'WHI-CHIVAS-12', 'Chivas Regal 12 ans 70cl', 'Chivas Regal 12 Years 70cl',
       c.id, s.id, 13000, 24000, 37, 40, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'ALC-WHI' AND s.code = 'SUP-PR';

-- Insert sample products (Champagne)
INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'CHA-MOET-IMP', 'Moët & Chandon Impérial 75cl', 'Moët & Chandon Impérial 75cl',
       c.id, s.id, 18000, 35000, 53, 58, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'ALC-CHA' AND s.code = 'SUP-MH';

INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'CHA-VEUVE-BRUT', 'Veuve Clicquot Brut 75cl', 'Veuve Clicquot Brut 75cl',
       c.id, s.id, 20000, 38000, 58, 63, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'ALC-CHA' AND s.code = 'SUP-MH';

-- Insert sample products (Cognac)
INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'COG-HENN-VS', 'Hennessy VS 70cl', 'Hennessy VS 70cl',
       c.id, s.id, 15000, 28000, 43, 47, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'ALC-COG' AND s.code = 'SUP-MH';

INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'COG-MART-VSOP', 'Martell VSOP 70cl', 'Martell VSOP 70cl',
       c.id, s.id, 18000, 33000, 50, 55, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'ALC-COG' AND s.code = 'SUP-PR';

-- Insert sample products (Parfums)
INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'PARF-DIOR-SAU', 'Dior Sauvage EDT 100ml', 'Dior Sauvage EDT 100ml',
       c.id, s.id, 25000, 45000, 69, 75, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'PARF' AND s.code = 'SUP-LVMH';

INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'PARF-CHANEL-5', 'Chanel N°5 EDP 100ml', 'Chanel N°5 EDP 100ml',
       c.id, s.id, 35000, 65000, 99, 108, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'PARF' AND s.code = 'SUP-LVMH';

INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'PARF-ARMANI-CODE', 'Armani Code Homme EDT 75ml', 'Armani Code Homme EDT 75ml',
       c.id, s.id, 20000, 38000, 58, 63, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'PARF' AND s.code = 'SUP-LOR';

-- Insert sample products (Cosmétiques)
INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'COSM-LANC-GENIF', 'Lancôme Génifique 50ml', 'Lancôme Génifique 50ml',
       c.id, s.id, 30000, 55000, 84, 92, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'COSM' AND s.code = 'SUP-LOR';

INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'COSM-DIOR-CAPTURE', 'Dior Capture Totale 50ml', 'Dior Capture Totale 50ml',
       c.id, s.id, 35000, 65000, 99, 108, 18.00, TRUE, TRUE, 'SYSTEM'
FROM categories c, suppliers s
WHERE c.code = 'COSM' AND s.code = 'SUP-LVMH';

-- Insert sample products (Confiserie)
INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'CONF-LINDT-ASST', 'Lindt Assortiment 500g', 'Lindt Assortment 500g',
       c.id, NULL, 5000, 9000, 14, 15, 5.50, TRUE, TRUE, 'SYSTEM'
FROM categories c
WHERE c.code = 'CONF';

INSERT INTO products (sku, name_fr, name_en, category_id, supplier_id, purchase_price, 
                     selling_price_xof, selling_price_eur, selling_price_usd, tax_rate, 
                     active, track_stock, created_by)
SELECT 'CONF-TOBLERONE', 'Toblerone 360g', 'Toblerone 360g',
       c.id, NULL, 3000, 6000, 9, 10, 5.50, TRUE, TRUE, 'SYSTEM'
FROM categories c
WHERE c.code = 'CONF';

-- Insert a sample promotion
INSERT INTO promotions (code, name, description, start_date, end_date, discount_type, 
                       discount_value, active, apply_to_all_products, created_by)
VALUES ('WELCOME10', 'Welcome 10% Off', 'Remise de bienvenue de 10%', 
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', 
        'PERCENTAGE', 10.00, TRUE, FALSE, 'SYSTEM');

-- Link promotion to whisky category
INSERT INTO promotion_categories (promotion_id, category_id)
SELECT p.id, c.id 
FROM promotions p, categories c
WHERE p.code = 'WELCOME10' AND c.code = 'ALC-WHI';

-- Update user passwords note
COMMENT ON TABLE users IS 'Default passwords: admin123, super123, caisse123, stock123 - All passwords are BCrypt hashed';

-- Insert audit log note
COMMENT ON COLUMN users.created_by IS 'User who created this record';
COMMENT ON COLUMN users.updated_by IS 'User who last updated this record';
COMMENT ON COLUMN users.deleted IS 'Soft delete flag - TRUE means record is deleted';