CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    event_version INTEGER NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ NULL
);

CREATE INDEX outbox_unpublished_idx
ON outbox_events(created_at)
WHERE published_at IS NULL;
