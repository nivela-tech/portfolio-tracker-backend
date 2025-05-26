-- changeset ami:1-create-portfolio_accounts
CREATE TABLE portfolio_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    relationship VARCHAR(255) NOT NULL,
    user_id BIGINT
);
