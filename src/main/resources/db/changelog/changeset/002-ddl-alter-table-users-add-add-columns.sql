--liquibase formatted sql

--changeset petrarsentev:alter_table_users_add-first-arg-to-users runOnChange:false
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS first_arg VARCHAR(2000) DEFAULT NULL;
--rollback ALTER TABLE public.users DROP COLUMN IF EXISTS first_arg;

--changeset petrarsentev:alter_table_users_add-second-arg-to-users runOnChange:false
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS second_arg VARCHAR(2000) DEFAULT NULL;
--rollback ALTER TABLE public.users DROP COLUMN IF EXISTS second_arg;

--changeset petrarsentev:alter_table_users_add-result-to-users runOnChange:false
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS result VARCHAR(2000) DEFAULT NULL;
--rollback ALTER TABLE public.users DROP COLUMN IF EXISTS result;
