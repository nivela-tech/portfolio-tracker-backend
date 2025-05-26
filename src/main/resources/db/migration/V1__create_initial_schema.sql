-- Combined initial schema for portfolio tracker

-- app_user table
CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    provider_id VARCHAR(255) UNIQUE,
    image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- portfolio_accounts table
CREATE TABLE IF NOT EXISTS portfolio_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    relationship VARCHAR(255) NOT NULL,
    user_id BIGINT,
    CONSTRAINT fk_portfolio_account_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- portfolio_entries table
CREATE TABLE IF NOT EXISTS portfolio_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'STOCK',
    source VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    country VARCHAR(255) NOT NULL,
    date_added TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    user_id BIGINT,
    CONSTRAINT fk_portfolio_entry_account FOREIGN KEY (account_id) REFERENCES portfolio_accounts(id),
    CONSTRAINT fk_portfolio_entry_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);
