-- Follow a schema per service
CREATE SCHEMA IF NOT EXISTS ordering;

CREATE TABLE ordering.ticket
(
    id         UUID PRIMARY KEY      DEFAULT uuidv7(),
    title      varchar(100) NOT NULL,
    price      varchar(10)  NOT NULL,
    version    INTEGER      NOT NULL DEFAULT 0,
    created_by varchar(50)  NOT NULL,
    created_at timestamptz  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by varchar(50)  NOT NULL,
    updated_at timestamptz  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ticket_idx ON ordering.ticket USING btree (title);
