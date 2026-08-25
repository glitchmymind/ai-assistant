package com.aiassistant.server.application.outbox

import java.time.Instant
import java.util.UUID

/**
 * Domain event persisted in the outbox and published to Kafka.
 *
 * Delivery is at-least-once: the same [id] may be published more than once if the process
 * crashes after Kafka acknowledges the produce and before `published_at` is written.
 * Consumers MUST treat [id] as an idempotency key and ignore duplicates.
 */
data class DomainEvent(
    val id: UUID,
    val aggregateType: String,
    val aggregateId: UUID,
    val eventType: String,
    val eventVersion: Int,
    val payload: String,
    val createdAt: Instant,
)

data class NewDomainEvent(
    val aggregateType: String,
    val aggregateId: UUID,
    val eventType: String,
    val eventVersion: Int,
    val payload: String,
)
