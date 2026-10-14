CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE payment.stripe_charge
(
    id         UUID PRIMARY KEY      DEFAULT uuidv7(),
    order_id   UUID         NOT NULL REFERENCES payment.order (id),
    stripe_id  VARCHAR(100) NOT NULL,
    version    INTEGER      NOT NULL DEFAULT 0,
    created_by VARCHAR(50)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX stripe_charge_order_idx ON payment.stripe_charge USING btree (order_id);
