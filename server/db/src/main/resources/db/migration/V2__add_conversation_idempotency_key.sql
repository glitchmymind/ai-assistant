ALTER TABLE conversations
ADD COLUMN idempotency_key UUID;

ALTER TABLE conversations
ADD CONSTRAINT conversations_idempotency_key_unique
UNIQUE (idempotency_key);
