package com.aiassistant.server.application.outbox

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Kafka message body for outbox events.
 *
 * [eventId] is the outbox row id and the consumer idempotency key.
 * Duplicate envelopes with the same [eventId] are expected under at-least-once delivery.
 */
@Serializable
data class EventEnvelope(
    val eventId: String,
    val eventVersion: Int,
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: JsonElement,
    val createdAt: String,
)
