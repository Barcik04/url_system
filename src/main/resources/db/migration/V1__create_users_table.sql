-- V1__create_users_table.sql
-- Creates users table + sequence for id generation

CREATE SEQUENCE user_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE users (
                                     id BIGINT PRIMARY KEY DEFAULT nextval('user_sequence'),
                                     username VARCHAR(100) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     role VARCHAR(50) NOT NULL DEFAULT 'USER'
);


