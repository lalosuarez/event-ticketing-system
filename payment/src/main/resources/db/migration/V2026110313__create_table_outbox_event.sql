CREATE TABLE payment.outbox_event
(
    id             UUID PRIMARY KEY         DEFAULT uuidv7(),
    aggregate_type VARCHAR(100)                                       NOT NULL,
    aggregate_id   VARCHAR(255)                                       NOT NULL,
    event_type     VARCHAR(100)                                       NOT NULL,
    payload        JSONB                                              NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status         VARCHAR(20)              DEFAULT 'PENDING'         NOT NULL,
    processed_at   TIMESTAMP WITH TIME ZONE,
    error_message  TEXT,
    retry_count    INT                      DEFAULT 0                 NOT NULL
);

-- Crucial: Create a partial index for the polling worker.
-- This keeps the index extremely small because it ignores processed messages.
--CREATE INDEX idx_outbox_pending ON payment.outbox_event (created_at ASC) WHERE status = 'PENDING';