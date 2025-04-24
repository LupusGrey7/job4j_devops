--liquibase formatted sql

--changeset petrarsentev:alter_table_users_add-create_date-to-users runOnChange:false
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS create_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now();

--rollback ALTER TABLE public.users DROP COLUMN IF EXISTS create_date;