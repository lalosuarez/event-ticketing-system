CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE payment.order
(
    id         UUID PRIMARY KEY       DEFAULT uuidv7(),
    amount     NUMERIC(9, 2) NOT NULL,
    status     VARCHAR(50)   NOT NULL,
    user_id    VARCHAR(50)   NOT NULL,
    version    INTEGER       NOT NULL DEFAULT 0,
    created_by VARCHAR(50)   NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)   NOT NULL,
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
