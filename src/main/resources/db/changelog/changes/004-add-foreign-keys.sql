-- changeset ami:2-add-foreign-keys
ALTER TABLE portfolio_accounts
ADD CONSTRAINT fk_portfolio_account_user FOREIGN KEY (user_id) REFERENCES app_user(id);

ALTER TABLE portfolio_entries
ADD CONSTRAINT fk_portfolio_entry_account FOREIGN KEY (account_id) REFERENCES portfolio_accounts(id);

ALTER TABLE portfolio_entries
ADD CONSTRAINT fk_portfolio_entry_user FOREIGN KEY (user_id) REFERENCES app_user(id);
