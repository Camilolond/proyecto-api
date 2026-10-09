-- Runs only the first time the PostgreSQL container starts with an empty volume
-- Each microservice owns its own database, the tables are created by Hibernate
CREATE DATABASE products_db;
CREATE DATABASE users_db;
