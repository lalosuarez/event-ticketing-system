-- Follow a schema per service
CREATE SCHEMA IF NOT EXISTS ordering;

CREATE TABLE ordering.ticket
(
    id         UUID PRIMARY KEY       DEFAULT uuidv7(),
    title      VARCHAR(100)  NOT NULL,
    price      NUMERIC(9, 2) NOT NULL,
    version    INTEGER       NOT NULL DEFAULT 0,
    created_by VARCHAR(50)   NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)   NOT NULL,
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ticket_idx ON ordering.ticket USING btree (title);
