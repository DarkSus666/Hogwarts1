-- liquibase formatted sql

-- changeset meliseev:1
CREATE INDEX student_name_index ON student (name);

-- changeset meliseev:2
CREATE INDEX faculty_nc_index ON faculty (name, color);