CREATE SCHEMA IF NOT EXISTS ordering;

CREATE TABLE ordering.ticket
(
    id                UUID PRIMARY KEY,
    title             VARCHAR(255)  NOT NULL,
    price             NUMERIC(9, 2) NOT NULL,
    -- Seller Information
    seller_id         VARCHAR(50)   NOT NULL,
    seller_first_name VARCHAR(255)  NOT NULL,
    seller_last_name  VARCHAR(255)  NOT NULL,
    -- Audit & Concurrency
    version           INTEGER       NOT NULL DEFAULT 0,
    created_by        VARCHAR(50)   NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(50)   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Used to check for users not buying their own tickets
CREATE INDEX ticket_idx ON ordering.ticket USING btree (seller_id);
