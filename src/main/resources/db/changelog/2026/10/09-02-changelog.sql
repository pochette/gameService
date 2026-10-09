-- liquibase formatted sql

-- changeset burda:1791578407758-1
CREATE TABLE users
(
    id         UUID                        NOT NULL,
    money      BIGINT                      NOT NULL,
    country    VARCHAR(255)                NOT NULL,
    state      JSONB                       NOT NULL,
    created_at TIMESTAMP(6) with time zone NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

-- changeset burda:1791578407758-2
CREATE INDEX idx_users_country_created_at ON users (country, created_at);

-- changeset burda:1791578407758-3
CREATE INDEX idx_users_country_money_id ON users (country, money DESC, id ASC);

