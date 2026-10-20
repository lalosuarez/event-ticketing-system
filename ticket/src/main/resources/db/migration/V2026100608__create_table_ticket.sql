CREATE SCHEMA IF NOT EXISTS ticketing;

CREATE TABLE ticketing.ticket
(
    id                UUID PRIMARY KEY       DEFAULT uuidv7(),
    title             VARCHAR(255)  NOT NULL,
    price             NUMERIC(9, 2) NOT NULL,
    status            VARCHAR(50)   NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'RESERVED', 'SOLD'
    order_id          UUID,                                       -- To check for reserved tickets
    -- Seller Information
    seller_id         VARCHAR(50)   NOT NULL,
    seller_email      VARCHAR(255)  NOT NULL,
    seller_first_name VARCHAR(255)  NOT NULL,
    seller_last_name  VARCHAR(255)  NOT NULL,
    -- Audit & Concurrency
    version           INTEGER       NOT NULL DEFAULT 0,
    created_by        VARCHAR(50)   NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(50)   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ticket_idx ON ticketing.ticket USING btree (title);
