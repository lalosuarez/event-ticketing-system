-- Follow a schema per service
CREATE SCHEMA IF NOT EXISTS ordering;

CREATE TABLE ordering.order
(
    id         UUID PRIMARY KEY     DEFAULT uuidv7(),
    user_id    varchar(50) NOT NULL,
    ticket_id  varchar(50) NOT NULL,
    status     varchar(50) NOT NULL,
    expires_at timestamptz NOT NULL,
    created_by varchar(50) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(50) NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX order_idx ON ordering.order USING btree (user_id);
CREATE UNIQUE INDEX unique_user_ticket_idx ON ordering.order (user_id, ticket_id);
