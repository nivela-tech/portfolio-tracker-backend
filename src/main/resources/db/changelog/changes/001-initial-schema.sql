--liquibase formatted sql
--changeset portfolio-tracker:001

-- 1. Enable UUID generation
-- The Railway PostgreSQL user should have superuser privileges to create extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2. Create app_user table
CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    provider_id VARCHAR(255) UNIQUE,
    image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- 3. Create portfolio_accounts table
CREATE TABLE portfolio_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    relationship VARCHAR(255) NOT NULL,
    user_id UUID,
    CONSTRAINT fk_portfolio_account_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- 4. Create portfolio_entries table
CREATE TABLE portfolio_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'STOCK',
    source VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    country VARCHAR(255) NOT NULL,
    date_added TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    user_id UUID,
    CONSTRAINT fk_portfolio_entry_account FOREIGN KEY (account_id) REFERENCES portfolio_accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_portfolio_entry_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- 5. Grant permissions
-- In Railway PostgreSQL, the connecting user already has all necessary permissions
-- The following is included for completeness when running in other environments
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO CURRENT_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO CURRENT_USER;
