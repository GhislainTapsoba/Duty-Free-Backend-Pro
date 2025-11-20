-- Migration V8: Add new fields to suppliers table
-- Purpose: Add postal_code, payment_terms, and credit_limit fields

-- Add postal_code field
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS postal_code VARCHAR(50);

-- Add payment_terms field
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(200);

-- Add credit_limit field
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS credit_limit DECIMAL(15, 2);

-- Comments
COMMENT ON COLUMN suppliers.postal_code IS 'Postal/ZIP code of the supplier';
COMMENT ON COLUMN suppliers.payment_terms IS 'Payment terms agreed with supplier (e.g., Net 30 days)';
COMMENT ON COLUMN suppliers.credit_limit IS 'Maximum credit limit allowed for this supplier';
