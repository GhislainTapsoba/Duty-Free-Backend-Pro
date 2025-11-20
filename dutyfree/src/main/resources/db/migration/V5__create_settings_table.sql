-- Create settings table
CREATE TABLE IF NOT EXISTS settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value TEXT,
    category VARCHAR(50),
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on category for faster queries
CREATE INDEX IF NOT EXISTS idx_settings_category ON settings(category);

-- Create index on key for faster lookups
CREATE INDEX IF NOT EXISTS idx_settings_key ON settings(setting_key);

-- Insert default settings
INSERT INTO settings (setting_key, setting_value, category, description) VALUES
('currency', 'XOF', 'general', 'Default currency'),
('taxRate', '18', 'general', 'Default tax rate percentage'),
('language', 'Français', 'general', 'Default language'),
('companyName', 'DJBC Duty Free', 'company', 'Company name'),
('address', 'Aéroport International de Ouagadougou', 'company', 'Company address'),
('phone', '+226 XX XX XX XX', 'company', 'Company phone'),
('taxId', '', 'company', 'Tax identification number')
ON CONFLICT (setting_key) DO NOTHING;

-- Add comment to table
COMMENT ON TABLE settings IS 'System configuration settings';
