-- V15__insert_test_admin_user.sql
-- This migration inserts a test admin user with password "password" (BCrypt hashed)
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy for password "password"
-- Actually using a different hash from https://bcrypt-generator.com/

DELETE FROM users WHERE username = 'testadmin';

INSERT INTO users (username, password, full_name, email, phone, role, active, created_by)
VALUES ('testadmin', '$2y$10$JO3U6F0.QxnKB3oDfL8gXOGmC2yJVPDPfqQJJ7eLKNV5/F7VvUBnC', 'Test Admin', 'test@dutyfree.com', '+22670000000', 'ADMIN', TRUE, 'SYSTEM');
