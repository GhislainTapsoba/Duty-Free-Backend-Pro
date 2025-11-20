-- Add notification settings
INSERT INTO settings (setting_key, setting_value, category, description) VALUES
('emailNotifications', 'true', 'notifications', 'Enable email notifications'),
('lowStockAlerts', 'true', 'notifications', 'Alert when products are low in stock'),
('expiringProductsAlerts', 'true', 'notifications', 'Alert for products approaching expiry'),
('dailySalesReport', 'false', 'notifications', 'Send daily sales report via email'),
('lowStockThreshold', '10', 'notifications', 'Threshold for low stock alerts'),
('expiryAlertDays', '30', 'notifications', 'Days before expiry to send alerts')
ON CONFLICT (setting_key) DO NOTHING;
