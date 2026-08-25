package com.aiassistant.server.application.outbox

import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OutboxPublisherTest {

    @Test
    fun `publishBatch sends unpublished events then marks them published`() = runBlocking {
        val event = sampleEvent()
        val outbox = FakeOutboxRepository(unpublished = mutableListOf(event))
        val publisher = RecordingEventPublisher()

        OutboxPublisher(outbox, publisher).publishBatch()

        assertEquals(listOf(event), publisher.published)
        assertEquals(listOf(event.id), outbox.marked)
        assertTrue(outbox.unpublished.isEmpty())
    }

    @Test
    fun `kafka failure leaves the outbox row unpublished`() = runBlocking {
        val event = sampleEvent()
        val outbox = FakeOutboxRepository(unpublished = mutableListOf(event))
        val publisher = RecordingEventPublisher(failOn = setOf(event.id))

        assertFailsWith<IllegalStateException> {
            OutboxPublisher(outbox, publisher).publishBatch()
        }

        assertTrue(publisher.published.isEmpty())
        assertTrue(outbox.marked.isEmpty())
        assertEquals(listOf(event), outbox.unpublished)
    }

    @Test
    fun `markPublished failure after kafka ack is retried as a duplicate publication`() = runBlocking {
        val event = sampleEvent()
        val outbox = FakeOutboxRepository(unpublished = mutableListOf(event))
        val publisher = RecordingEventPublisher()
        val worker = OutboxPublisher(outbox, publisher)

        outbox.failMarkOn = setOf(event.id)
        assertFailsWith<IllegalStateException> {
            worker.publishBatch()
        }
        assertEquals(listOf(event), publisher.published)
        assertEquals(listOf(event), outbox.unpublished)

        outbox.failMarkOn = emptySet()
        worker.publishBatch()

        assertEquals(listOf(event, event), publisher.published)
        assertEquals(listOf(event.id), outbox.marked)
        assertTrue(outbox.unpublished.isEmpty())
    }

    @Test
    fun `consumers ignore a duplicate eventId`() {
        val event = sampleEvent()
        val consumer = IdempotentConsumer()

        assertTrue(consumer.consume(event))
        assertFalse(consumer.consume(event))
        assertEquals(listOf(event.id), consumer.processed)
    }
}

private fun sampleEvent() = DomainEvent(
    id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
    aggregateType = "Conversation",
    aggregateId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
    eventType = "ConversationCreated",
    eventVersion = 1,
    payload = """{"id":"123e4567-e89b-12d3-a456-426614174000","title":"Planning","createdAt":"2026-08-21T11:05:00Z"}""",
    createdAt = Instant.parse("2026-08-21T11:05:00Z"),
)

private class FakeOutboxRepository(
    val unpublished: MutableList<DomainEvent>,
    var failMarkOn: Set<UUID> = emptySet(),
) : OutboxRepository {
    val marked = mutableListOf<UUID>()

    override suspend fun insert(event: NewDomainEvent) {
        error("unused")
    }

    override suspend fun findUnpublished(limit: Int): List<DomainEvent> = unpublished.take(limit)

    override suspend fun markPublished(id: UUID) {
        if (id in failMarkOn) error("mark published failed")
        unpublished.removeAll { it.id == id }
        marked += id
    }
}

private class RecordingEventPublisher(
    private val failOn: Set<UUID> = emptySet(),
) : EventPublisher {
    val published = mutableListOf<DomainEvent>()

    override suspend fun publish(event: DomainEvent) {
        if (event.id in failOn) error("kafka unavailable")
        published += event
    }
}

private class IdempotentConsumer {
    val processed = mutableListOf<UUID>()
    private val seen = mutableSetOf<UUID>()

    fun consume(event: DomainEvent): Boolean {
        if (!seen.add(event.id)) {
            return false
        }
        processed += event.id
        return true
    }
}
