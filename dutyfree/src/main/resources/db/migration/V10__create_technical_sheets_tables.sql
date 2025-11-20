-- Migration V10: Create tables for technical sheets and raw materials management
-- Raw materials (matières premières), technical sheets (fiches techniques), and their items

-- Table: raw_materials
CREATE TABLE IF NOT EXISTS raw_materials (
    id BIGSERIAL PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL UNIQUE,
    material_name VARCHAR(200) NOT NULL,
    description TEXT,
    material_category VARCHAR(50) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    purchase_price DECIMAL(19, 2) NOT NULL,
    quantity_in_stock DECIMAL(19, 3) NOT NULL DEFAULT 0,
    min_stock_level DECIMAL(19, 3),
    reorder_level DECIMAL(19, 3),
    reorder_quantity DECIMAL(19, 3),
    supplier_name VARCHAR(200),
    supplier_contact VARCHAR(100),
    perishable BOOLEAN NOT NULL DEFAULT FALSE,
    shelf_life_days INTEGER,
    storage_conditions VARCHAR(100),
    last_purchase_date DATE,
    last_purchase_price DECIMAL(19, 2),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- Table: technical_sheets
CREATE TABLE IF NOT EXISTS technical_sheets (
    id BIGSERIAL PRIMARY KEY,
    sheet_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    product_id BIGINT UNIQUE,
    version VARCHAR(20) DEFAULT '1.0',
    output_quantity DECIMAL(19, 3) NOT NULL DEFAULT 1,
    output_unit VARCHAR(20) NOT NULL DEFAULT 'PIECE',
    preparation_time INTEGER,
    cooking_time INTEGER,
    difficulty VARCHAR(20) DEFAULT 'MEDIUM',
    instructions TEXT,
    total_cost DECIMAL(19, 2) DEFAULT 0,
    cost_per_unit DECIMAL(19, 2) DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    validated BOOLEAN NOT NULL DEFAULT FALSE,
    validated_at TIMESTAMP,
    validated_by VARCHAR(50),
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_technical_sheet_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- Table: technical_sheet_items
CREATE TABLE IF NOT EXISTS technical_sheet_items (
    id BIGSERIAL PRIMARY KEY,
    technical_sheet_id BIGINT NOT NULL,
    raw_material_id BIGINT NOT NULL,
    quantity DECIMAL(19, 3) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    cost DECIMAL(19, 2),
    display_order INTEGER DEFAULT 0,
    notes VARCHAR(500),
    optional BOOLEAN NOT NULL DEFAULT FALSE,
    conversion_factor DECIMAL(19, 6) DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_tsi_technical_sheet FOREIGN KEY (technical_sheet_id) REFERENCES technical_sheets(id) ON DELETE CASCADE,
    CONSTRAINT fk_tsi_raw_material FOREIGN KEY (raw_material_id) REFERENCES raw_materials(id) ON DELETE RESTRICT
);

-- Indexes for raw_materials
CREATE INDEX IF NOT EXISTS idx_raw_material_code ON raw_materials(material_code);
CREATE INDEX IF NOT EXISTS idx_raw_material_category ON raw_materials(material_category);
CREATE INDEX IF NOT EXISTS idx_raw_material_active ON raw_materials(active);
CREATE INDEX IF NOT EXISTS idx_raw_material_deleted ON raw_materials(deleted);

-- Indexes for technical_sheets
CREATE INDEX IF NOT EXISTS idx_technical_sheet_code ON technical_sheets(sheet_code);
CREATE INDEX IF NOT EXISTS idx_technical_sheet_product ON technical_sheets(product_id);
CREATE INDEX IF NOT EXISTS idx_technical_sheet_active ON technical_sheets(active);
CREATE INDEX IF NOT EXISTS idx_technical_sheet_validated ON technical_sheets(validated);
CREATE INDEX IF NOT EXISTS idx_technical_sheet_deleted ON technical_sheets(deleted);

-- Indexes for technical_sheet_items
CREATE INDEX IF NOT EXISTS idx_tsi_technical_sheet ON technical_sheet_items(technical_sheet_id);
CREATE INDEX IF NOT EXISTS idx_tsi_raw_material ON technical_sheet_items(raw_material_id);
CREATE INDEX IF NOT EXISTS idx_tsi_display_order ON technical_sheet_items(display_order);

-- Triggers for updated_at timestamp
CREATE OR REPLACE FUNCTION update_raw_materials_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_raw_materials_updated_at
    BEFORE UPDATE ON raw_materials
    FOR EACH ROW
    EXECUTE FUNCTION update_raw_materials_updated_at();

CREATE OR REPLACE FUNCTION update_technical_sheets_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_technical_sheets_updated_at
    BEFORE UPDATE ON technical_sheets
    FOR EACH ROW
    EXECUTE FUNCTION update_technical_sheets_updated_at();

CREATE OR REPLACE FUNCTION update_technical_sheet_items_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_technical_sheet_items_updated_at
    BEFORE UPDATE ON technical_sheet_items
    FOR EACH ROW
    EXECUTE FUNCTION update_technical_sheet_items_updated_at();

-- Comments
COMMENT ON TABLE raw_materials IS 'Matières premières et ingrédients utilisés dans les fiches techniques';
COMMENT ON TABLE technical_sheets IS 'Fiches techniques (recettes/compositions) des produits';
COMMENT ON TABLE technical_sheet_items IS 'Ingrédients/matières premières utilisés dans chaque fiche technique';

COMMENT ON COLUMN raw_materials.material_code IS 'Code unique de la matière première';
COMMENT ON COLUMN raw_materials.material_category IS 'Catégorie: FOOD, BEVERAGE, PACKAGING, CLEANING, OTHER';
COMMENT ON COLUMN raw_materials.unit IS 'Unité de base: KG, L, G, ML, PIECE, etc.';
COMMENT ON COLUMN raw_materials.perishable IS 'Si la matière est périssable';
COMMENT ON COLUMN raw_materials.shelf_life_days IS 'Durée de conservation en jours';

COMMENT ON COLUMN technical_sheets.sheet_code IS 'Code unique de la fiche technique';
COMMENT ON COLUMN technical_sheets.difficulty IS 'Niveau de difficulté: EASY, MEDIUM, HARD';
COMMENT ON COLUMN technical_sheets.output_quantity IS 'Quantité produite par cette recette';
COMMENT ON COLUMN technical_sheets.output_unit IS 'Unité de sortie: PIECE, KG, L, etc.';
COMMENT ON COLUMN technical_sheets.validated IS 'Si la fiche technique est validée';

COMMENT ON COLUMN technical_sheet_items.conversion_factor IS 'Facteur de conversion si l''unité diffère de l''unité de base de la matière première';
COMMENT ON COLUMN technical_sheet_items.optional IS 'Si l''ingrédient est optionnel';
COMMENT ON COLUMN technical_sheet_items.display_order IS 'Ordre d''affichage dans la recette';
