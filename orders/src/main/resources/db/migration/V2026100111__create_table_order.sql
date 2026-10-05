-- Follow a schema per service
CREATE SCHEMA IF NOT EXISTS ordering;

CREATE TABLE ordering.order
(
    id         UUID PRIMARY KEY     DEFAULT uuidv7(),
    user_id    VARCHAR(50) NOT NULL,
    ticket_id  UUID        NOT NULL REFERENCES ordering.ticket (id),
    status     VARCHAR(50) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    version    INTEGER     NOT NULL DEFAULT 0,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX order_idx ON ordering.order USING btree (user_id);

-- If a ticket_id can only ever belong to one user in the entire system
-- By having ticket_id first PostgreSQL can use this single index to optimize queries
-- for ticket_id alone and queries using both columns.
CREATE UNIQUE INDEX unique_ticket_user_idx ON ordering.order (ticket_id, user_id) WHERE status != 'CANCELLED';


/*CREATE
    OR REPLACE FUNCTION increment_version()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.version
        = COALESCE(OLD.version, 0) + 1;
    RETURN NEW;
END;
$$
    LANGUAGE plpgsql;

CREATE TRIGGER increment_order_version_trigger
    BEFORE UPDATE
    ON ordering.ticket
    FOR EACH ROW
EXECUTE FUNCTION increment_version();*/
