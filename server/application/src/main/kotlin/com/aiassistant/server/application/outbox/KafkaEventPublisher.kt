package com.aiassistant.server.application.outbox

import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter

class KafkaEventPublisher(
    private val sender: KafkaMessageSender,
    private val topic: String,
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    },
) : EventPublisher {
    override suspend fun publish(event: DomainEvent) {
        val envelope = EventEnvelope(
            eventId = event.id.toString(),
            eventVersion = event.eventVersion,
            aggregateType = event.aggregateType,
            aggregateId = event.aggregateId.toString(),
            eventType = event.eventType,
            payload = json.parseToJsonElement(event.payload),
            createdAt = DateTimeFormatter.ISO_INSTANT.format(event.createdAt),
        )
        sender.send(
            topic = topic,
            key = event.aggregateId.toString(),
            value = json.encodeToString(EventEnvelope.serializer(), envelope),
        )
    }
}
