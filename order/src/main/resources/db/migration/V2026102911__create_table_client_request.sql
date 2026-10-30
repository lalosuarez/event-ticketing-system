CREATE TABLE ordering.client_request
(
    id               VARCHAR(128) NOT NULL PRIMARY KEY,
    user_id          VARCHAR(50)  NOT NULL,
    request_hash     CHAR(64)     NOT NULL,                       -- SHA-256 hexadecimal string
    response_payload JSONB,
    status           VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED, FAILED
    version          INTEGER      NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    locked_until     TIMESTAMPTZ  DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 seconds')
);

-- Uniqueness per user (defense in depth)
CREATE UNIQUE INDEX uq_client_request_user ON ordering.client_request (user_id, id);

-- Index for TTL / retention cleanup jobs
CREATE INDEX idx_client_mutation_created_at ON ordering.client_request (created_at);

-- Partial index for fast lock lookups
CREATE INDEX idx_client_request_lock ON ordering.client_request (id, status, locked_until);