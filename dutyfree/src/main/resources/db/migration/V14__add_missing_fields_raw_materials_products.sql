-- Migration V14: Add missing fields to raw_materials and products tables
-- Created: 2025-11-18
-- Description: Adds supplier info, reorder quantity, purchase history to raw_materials
--              and product_code to products

-- ============================================
-- 1. Add missing fields to raw_materials table
-- ============================================

-- Rename 'name' column to 'material_name' if needed
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'raw_materials' AND column_name = 'name'
    ) THEN
        ALTER TABLE raw_materials RENAME COLUMN name TO material_name;
    END IF;
END $$;

-- Add supplier information columns
ALTER TABLE raw_materials ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(200);
ALTER TABLE raw_materials ADD COLUMN IF NOT EXISTS supplier_contact VARCHAR(100);

-- Add reorder quantity
ALTER TABLE raw_materials ADD COLUMN IF NOT EXISTS reorder_quantity DECIMAL(19,3) DEFAULT 0;

-- Add purchase history
ALTER TABLE raw_materials ADD COLUMN IF NOT EXISTS last_purchase_date DATE;
ALTER TABLE raw_materials ADD COLUMN IF NOT EXISTS last_purchase_price DECIMAL(19,2);

-- Add index on material_name if not exists
CREATE INDEX IF NOT EXISTS idx_raw_material_name ON raw_materials(material_name);

-- ============================================
-- 2. Add missing fields to products table
-- ============================================

-- Add product_code column
ALTER TABLE products ADD COLUMN IF NOT EXISTS product_code VARCHAR(100);

-- Update product_code with sku value for existing records (if null)
UPDATE products SET product_code = sku WHERE product_code IS NULL;

-- Make product_code NOT NULL and UNIQUE
ALTER TABLE products ALTER COLUMN product_code SET NOT NULL;

-- Add unique constraint on product_code (using DO block to check if exists)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT constraint_name FROM information_schema.table_constraints
        WHERE table_name = 'products' AND constraint_type = 'UNIQUE' AND constraint_name = 'uk_product_code'
    ) THEN
        ALTER TABLE products ADD CONSTRAINT uk_product_code UNIQUE (product_code);
    END IF;
END $$;

-- Add index on product_code
CREATE INDEX IF NOT EXISTS idx_product_code ON products(product_code);

-- ============================================
-- 3. Update table comments
-- ============================================

COMMENT ON COLUMN raw_materials.material_name IS 'Nom de la matière première';
COMMENT ON COLUMN raw_materials.supplier_name IS 'Nom du fournisseur';
COMMENT ON COLUMN raw_materials.supplier_contact IS 'Contact du fournisseur';
COMMENT ON COLUMN raw_materials.reorder_quantity IS 'Quantité de réapprovisionnement recommandée';
COMMENT ON COLUMN raw_materials.last_purchase_date IS 'Date du dernier achat';
COMMENT ON COLUMN raw_materials.last_purchase_price IS 'Prix du dernier achat';

COMMENT ON COLUMN products.product_code IS 'Code unique du produit';
