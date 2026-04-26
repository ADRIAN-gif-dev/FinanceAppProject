| table_name   | column_name       | data_type                | is_nullable |
| ------------ | ----------------- | ------------------------ | ----------- |
| budgets      | user_id           | uuid                     | YES         |
| budgets      | monthly_limit     | numeric                  | NO          |
| budgets      | current_spend     | numeric                  | YES         |
| budgets      | category_name     | character varying        | NO          |
| budgets      | budget_id         | uuid                     | NO          |
| debts        | total_amount      | numeric                  | NO          |
| debts        | debt_id           | uuid                     | NO          |
| debts        | due_date          | date                     | NO          |
| debts        | status            | character varying        | YES         |
| debts        | debt_name         | character varying        | NO          |
| debts        | user_id           | uuid                     | YES         |
| debts        | remaining_balance | numeric                  | NO          |
| funds        | is_achieved       | boolean                  | YES         |
| funds        | fund_id           | uuid                     | NO          |
| funds        | user_id           | uuid                     | YES         |
| funds        | goal_amount       | numeric                  | NO          |
| funds        | current_balance   | numeric                  | YES         |
| funds        | fund_name         | character varying        | NO          |
| reports      | smart_tips        | text                     | YES         |
| reports      | user_id           | uuid                     | YES         |
| reports      | report_id         | uuid                     | NO          |
| reports      | debt_trend        | text                     | YES         |
| reports      | month             | character varying        | NO          |
| transactions | trans_id          | uuid                     | NO          |
| transactions | type              | character varying        | NO          |
| transactions | source_id         | uuid                     | NO          |
| transactions | user_id           | uuid                     | YES         |
| transactions | timestamp         | timestamp with time zone | YES         |
| transactions | amount            | numeric                  | NO          |
| users        | created_at        | timestamp with time zone | YES         |
| users        | user_id           | uuid                     | NO          |
| users        | email             | character varying        | NO          |
| users        | password_hash     | character varying        | NO          |
| users        | biometric_token   | text                     | YES         |
| users        | name              | character varying        | NO          |