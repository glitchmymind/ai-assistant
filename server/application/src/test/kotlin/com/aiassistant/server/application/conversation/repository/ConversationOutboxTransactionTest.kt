package com.aiassistant.server.application.conversation.repository

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationInsert
import com.aiassistant.server.application.conversation.event.toCreatedEvent
import com.aiassistant.server.application.outbox.NewDomainEvent
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ConversationOutboxTransactionTest {

    @Test
    fun `create writes the conversation and outbox event in one transaction`() = runBlocking {
        val store = InMemoryTransactionalStore()

        val result = store.create("Planning", UUID.randomUUID())

        assertIs<ConversationInsert.Inserted>(result)
        assertEquals(listOf(result.conversation), store.conversations)
        assertEquals(1, store.events.size)
        assertEquals(result.conversation.id, store.events.single().aggregateId)
        assertEquals("ConversationCreated", store.events.single().eventType)
        assertEquals(1, store.events.single().eventVersion)
    }

    @Test
    fun `outbox failure rolls back the conversation insert`() = runBlocking {
        val store = InMemoryTransactionalStore(failOutbox = true)

        assertFailsWith<IllegalStateException> {
            store.create("Planning", UUID.randomUUID())
        }

        assertTrue(store.conversations.isEmpty())
        assertTrue(store.events.isEmpty())
    }

    @Test
    fun `conversation failure does not persist an outbox event`() = runBlocking {
        val store = InMemoryTransactionalStore(failConversation = true)

        assertFailsWith<IllegalStateException> {
            store.create("Planning", UUID.randomUUID())
        }

        assertTrue(store.conversations.isEmpty())
        assertTrue(store.events.isEmpty())
    }

    @Test
    fun `duplicate idempotency key does not insert a second outbox event`() = runBlocking {
        val store = InMemoryTransactionalStore()
        val key = UUID.randomUUID()
        val first = store.create("Planning", key)
        assertIs<ConversationInsert.Inserted>(first)

        val second = store.create("Planning", key)

        assertIs<ConversationInsert.AlreadyExists>(second)
        assertEquals(first.conversation, second.conversation)
        assertEquals(1, store.conversations.size)
        assertEquals(1, store.events.size)
    }
}

private class InMemoryTransactionalStore(
    private val failOutbox: Boolean = false,
    private val failConversation: Boolean = false,
) {
    val conversations = mutableListOf<Conversation>()
    val events = mutableListOf<NewDomainEvent>()
    private val byIdempotencyKey = mutableMapOf<UUID, UUID>()

    suspend fun create(title: String, idempotencyKey: UUID): ConversationInsert = transaction {
        writeCreatedConversation(
            title = title,
            idempotencyKey = idempotencyKey,
            insertConversation = { normalizedTitle, key ->
                when {
                    failConversation -> error("conversation insert failed")
                    byIdempotencyKey.containsKey(key) -> null
                    else -> Conversation(
                        id = UUID.randomUUID(),
                        title = normalizedTitle,
                        createdAt = Instant.parse("2026-08-21T11:05:00Z"),
                    ).also { conversation ->
                        conversations += conversation
                        byIdempotencyKey[key] = conversation.id
                    }
                }
            },
            insertOutbox = { conversation ->
                if (failOutbox) error("outbox insert failed")
                events += conversation.toCreatedEvent()
            },
            findExisting = { key ->
                val id = byIdempotencyKey.getValue(key)
                conversations.first { it.id == id }
            },
        )
    }

    private suspend fun <T> transaction(block: suspend () -> T): T {
        val conversationSnapshot = conversations.toList()
        val eventSnapshot = events.toList()
        val keySnapshot = byIdempotencyKey.toMap()
        return try {
            block()
        } catch (error: Throwable) {
            conversations.clear()
            conversations.addAll(conversationSnapshot)
            events.clear()
            events.addAll(eventSnapshot)
            byIdempotencyKey.clear()
            byIdempotencyKey.putAll(keySnapshot)
            throw error
        }
    }
}
