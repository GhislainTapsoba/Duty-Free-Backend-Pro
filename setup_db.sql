-- Créer la base de données et l'utilisateur
CREATE DATABASE dutyfree_db;
CREATE USER postgres WITH PASSWORD 'admin';
GRANT ALL PRIVILEGES ON DATABASE dutyfree_db TO postgres;
ALTER USER postgres CREATEDB;