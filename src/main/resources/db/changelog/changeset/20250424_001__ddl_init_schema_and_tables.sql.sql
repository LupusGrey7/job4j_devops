--liquibase formatted sql

--changeset petrarsentev:create_schema runOnChange:false
CREATE SCHEMA IF NOT EXISTS public;
--rollback DROP SCHEMA IF EXISTS public;

--changeset petrarsentev:drop-users runOnChange:false
--comment: Drop schema objects in dev/test for easy recreation
DROP SEQUENCE IF EXISTS public.hibernate_sequence;
DROP TABLE IF EXISTS public.roles CASCADE;
DROP TABLE IF EXISTS public.users CASCADE;
--rollback not required

--changeset petrarsentev:create-hibernate-sequence runOnChange:false
--comment: Create hibernate_sequence
CREATE SEQUENCE IF NOT EXISTS public.hibernate_sequence
    AS BIGINT          -- Уточнение типа
    START WITH 10
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1
    CYCLE;             -- Можно убрать, если не хотим, чтобы значения повторялись
--rollback DROP SEQUENCE IF EXISTS public.hibernate_sequence;

--changeset petrarsentev:create-users-table runOnChange:false
CREATE TABLE IF NOT EXISTS public.users (
    id BIGINT NOT NULL PRIMARY KEY,
    username VARCHAR(255)
);
--rollback DROP TABLE IF EXISTS public.users;

--changeset petrarsentev:create-roles-table runOnChange:false
CREATE TABLE IF NOT EXISTS public.roles (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id BIGINT,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);
--rollback DROP TABLE IF EXISTS public.roles;