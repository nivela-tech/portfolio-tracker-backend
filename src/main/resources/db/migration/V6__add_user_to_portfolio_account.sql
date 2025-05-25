ALTER TABLE portfolio_accounts ADD COLUMN user_id BIGINT;
ALTER TABLE portfolio_accounts ADD CONSTRAINT fk_portfolio_account_user FOREIGN KEY (user_id) REFERENCES app_user(id);
