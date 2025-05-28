-- PostgreSQL Setup Script for Portfolio Tracker Application
-- This script combines all the necessary SQL commands to set up your database manually

-- Create the database and user (run as postgres user)
--CREATE USER portfolio_user WITH PASSWORD 'portfolio@123';
--CREATE DATABASE portfolio_db;
--GRANT ALL PRIVILEGES ON DATABASE portfolio_db TO portfolio_user;

-- Connect to portfolio_db and run the following (as portfolio_user)

-- 1. Create app_user table
CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    provider_id VARCHAR(255) UNIQUE,
    image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

ALTER TABLE app_user OWNER TO portfolio_user;

-- 2. Create portfolio_accounts table
CREATE TABLE portfolio_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    relationship VARCHAR(255) NOT NULL,
    user_id UUID, -- Changed to UUID
    CONSTRAINT fk_portfolio_account_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE -- Added ON DELETE CASCADE
);

ALTER TABLE portfolio_accounts OWNER TO portfolio_user;

-- 3. Create portfolio_entries table
CREATE TABLE portfolio_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL, -- Changed to UUID
    type VARCHAR(20) NOT NULL DEFAULT 'STOCK',
    source VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    country VARCHAR(255) NOT NULL,
    date_added TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    user_id UUID, -- Changed to UUID
    CONSTRAINT fk_portfolio_entry_account FOREIGN KEY (account_id) REFERENCES portfolio_accounts(id) ON DELETE CASCADE, -- Added ON DELETE CASCADE
    CONSTRAINT fk_portfolio_entry_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE -- Added ON DELETE CASCADE
);

ALTER TABLE portfolio_entries OWNER TO portfolio_user;

-- 4. Add foreign key constraints
-- Constraints are now defined directly in the table creation for clarity and atomicity.
-- ALTER TABLE portfolio_accounts
-- ADD CONSTRAINT fk_portfolio_account_user FOREIGN KEY (user_id) REFERENCES app_user(id);

-- ALTER TABLE portfolio_entries
-- ADD CONSTRAINT fk_portfolio_entry_account FOREIGN KEY (account_id) REFERENCES portfolio_accounts(id);

-- ALTER TABLE portfolio_entries
-- ADD CONSTRAINT fk_portfolio_entry_user FOREIGN KEY (user_id) REFERENCES app_user(id);

-- Grant necessary permissions (run as portfolio_user)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO portfolio_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO portfolio_user;
