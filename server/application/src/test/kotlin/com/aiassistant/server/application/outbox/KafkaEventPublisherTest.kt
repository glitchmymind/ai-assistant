package com.aiassistant.server.application.outbox

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class KafkaEventPublisherTest {

    @Test
    fun `publishes to conversation-events with conversationId key and envelope metadata`() = runBlocking {
        val sender = RecordingKafkaMessageSender()
        val publisher = KafkaEventPublisher(
            sender = sender,
            topic = "conversation-events",
        )
        val event = DomainEvent(
            id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            aggregateType = "Conversation",
            aggregateId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            eventType = "ConversationCreated",
            eventVersion = 1,
            payload = """{"id":"123e4567-e89b-12d3-a456-426614174000","title":"Planning","createdAt":"2026-08-21T11:05:00Z"}""",
            createdAt = Instant.parse("2026-08-21T11:05:00Z"),
        )

        publisher.publish(event)

        val sent = sender.records.single()
        assertEquals("conversation-events", sent.topic)
        assertEquals(event.aggregateId.toString(), sent.key)

        val envelope = Json.decodeFromString(EventEnvelope.serializer(), sent.value)
        assertEquals(event.id.toString(), envelope.eventId)
        assertEquals(1, envelope.eventVersion)
        assertEquals("Conversation", envelope.aggregateType)
        assertEquals(event.aggregateId.toString(), envelope.aggregateId)
        assertEquals("ConversationCreated", envelope.eventType)
        assertEquals("2026-08-21T11:05:00Z", envelope.createdAt)
    }
}

private class RecordingKafkaMessageSender : KafkaMessageSender {
    val records = mutableListOf<SentRecord>()

    override suspend fun send(topic: String, key: String, value: String) {
        records += SentRecord(topic, key, value)
    }
}

private data class SentRecord(
    val topic: String,
    val key: String,
    val value: String,
)
