--liquibase formatted sql
--changeset portfolio-tracker:002

-- Add index on user_id in portfolio_accounts table
CREATE INDEX IF NOT EXISTS idx_portfolio_accounts_user_id ON portfolio_accounts (user_id);

-- Add index on account_id in portfolio_entries table
CREATE INDEX IF NOT EXISTS idx_portfolio_entries_account_id ON portfolio_entries (account_id);

-- Add index on user_id in portfolio_entries table
CREATE INDEX IF NOT EXISTS idx_portfolio_entries_user_id ON portfolio_entries (user_id);
