-- Add user_id to portfolio_accounts table
ALTER TABLE portfolio_accounts
ADD COLUMN user_id BIGINT;

-- Add foreign key constraint to portfolio_accounts referencing app_user
ALTER TABLE portfolio_accounts
ADD CONSTRAINT fk_user_id
FOREIGN KEY (user_id)
REFERENCES app_user(id);
