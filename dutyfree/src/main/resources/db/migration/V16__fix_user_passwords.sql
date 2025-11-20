-- V16__fix_user_passwords.sql
-- Fix all user passwords with correct BCrypt hashes
-- TEMPORARY: All passwords are set to "password" for initial login
-- Hash for "password": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- Users should change their passwords after first login

-- Update all users to use "password" as temporary password
UPDATE users
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE username IN ('admin', 'superviseur', 'caissier', 'stockmgr', 'testadmin');

-- Log the update
COMMENT ON TABLE users IS 'Updated 2025-11-20: All users have temporary password "password". Users should change their passwords after login. Default credentials: admin/password, superviseur/password, caissier/password, stockmgr/password, testadmin/password';
