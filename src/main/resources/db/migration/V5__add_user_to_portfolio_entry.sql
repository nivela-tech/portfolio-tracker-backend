ALTER TABLE portfolio_entries ADD COLUMN user_id BIGINT;
ALTER TABLE portfolio_entries ADD CONSTRAINT fk_portfolio_entry_user FOREIGN KEY (user_id) REFERENCES app_user(id);
