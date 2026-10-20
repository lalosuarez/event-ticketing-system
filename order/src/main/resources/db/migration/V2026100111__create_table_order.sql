CREATE SCHEMA IF NOT EXISTS ordering;

CREATE TABLE ordering.order
(
    id                UUID PRIMARY KEY       DEFAULT uuidv7(),
    -- Ticket Snapshot
    ticket_id         UUID          NOT NULL,
    ticket_title      VARCHAR(255)  NOT NULL,
    ticket_price      NUMERIC(9, 2) NOT NULL,
    quantity          INTEGER       NOT NULL,
    total_amount      NUMERIC(9, 2) NOT NULL,
    -- Seller Snapshot
    seller_id         VARCHAR(50)   NOT NULL,
    seller_first_name VARCHAR(255)  NOT NULL,
    seller_last_name  VARCHAR(255)  NOT NULL,
    -- Buyer Snapshot
    buyer_id          VARCHAR(50)   NOT NULL,
    buyer_first_name  VARCHAR(255)  NOT NULL,
    buyer_last_name   VARCHAR(255)  NOT NULL,
    -- Order State
    status            VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    expires_at        TIMESTAMPTZ   NOT NULL,
    -- Concurrency & Audit
    version           INTEGER       NOT NULL DEFAULT 0,
    created_by        VARCHAR(50)   NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(50)   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX order_idx ON ordering.order USING btree (buyer_id);

-- If a ticket_id can only ever belong to one user in the entire system
-- By having ticket_id first PostgreSQL can use this single index to optimize queries
-- for ticket_id alone and queries using both columns.
CREATE UNIQUE INDEX unique_ticket_user_idx ON ordering.order (ticket_id, buyer_id) WHERE status != 'CANCELLED';
