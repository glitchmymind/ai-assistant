package com.aiassistant.server.application.outbox

/**
 * Publishes a persisted outbox event to the message broker.
 *
 * Implementations MUST throw if the broker does not acknowledge the produce so the outbox
 * row stays unpublished and can be retried. Retries can deliver the same event more than
 * once; consumers must be idempotent on [DomainEvent.id].
 */
fun interface EventPublisher {
    suspend fun publish(event: DomainEvent)
}
