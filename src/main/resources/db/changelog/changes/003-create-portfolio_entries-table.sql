-- changeset ami:1-create-portfolio_entries
CREATE TABLE portfolio_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'STOCK',
    source VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    country VARCHAR(255) NOT NULL,
    date_added TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    user_id BIGINT
);
