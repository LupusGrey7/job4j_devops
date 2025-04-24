--liquibase formatted sql

--changeset petrarsentev:create_schema runOnChange:true
CREATE SCHEMA IF NOT EXISTS public;

--changeset petrarsentev:drop_users runAlways:true context:dev,test
-- Drop in test/dev to recreate schema easily
DROP SEQUENCE IF EXISTS public.hibernate_sequence;
DROP TABLE IF EXISTS public.users CASCADE;
DROP TABLE IF EXISTS public.roles CASCADE;

--changeset petrarsentev:create_sequence
CREATE SEQUENCE IF NOT EXISTS public.hibernate_sequence
    START WITH 10
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

--changeset petrarsentev:create_users_table
CREATE TABLE IF NOT EXISTS public.users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(2000)
);

--changeset petrarsentev:create_roles_table
CREATE TABLE IF NOT EXISTS public.roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id BIGINT,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);

