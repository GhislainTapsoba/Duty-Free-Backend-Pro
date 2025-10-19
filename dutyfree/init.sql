-- init-scripts/01-init.sql

-- Create database if not exists
SELECT 'CREATE DATABASE dutyfree_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'dutyfree_db')\gexec

-- Enable required extensions
\c dutyfree_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE dutyfree_db TO postgres;