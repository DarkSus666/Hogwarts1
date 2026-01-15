-- liquibase formatted sql

-- changeset meliseev:1
CREATE TABLE users (
id SERIAL,
email TEXT
)

-- changeset meliseev:2
ALTER TABLE users
ADD COLUMN name TEXT;

-- changeset meliseev:3
CREATE INDEX users_name_index ON users (name);