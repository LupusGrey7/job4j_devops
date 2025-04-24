--liquibase formatted sql

--changeset petrarsentev:alter_table_items_add_checked_column runOnChange:true
ALTER TABLE users ADD COLUMN first_arg VARCHAR(2000) DEFAULT NULL;
ALTER TABLE users ADD COLUMN second_arg VARCHAR(2000) DEFAULT NULL;
ALTER TABLE users ADD COLUMN result VARCHAR(2000) DEFAULT NULL;
