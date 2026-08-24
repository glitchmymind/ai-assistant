package com.aiassistant.server.application.conversation

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationCache
import com.aiassistant.server.application.conversation.domain.ConversationInsert
import com.aiassistant.server.application.conversation.domain.ConversationRepository
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetConversationUseCaseTest {

    @Test
    fun `cache hit skips the database`() = runBlocking {
        val conversation = sampleConversation()
        val repository = FakeConversationRepository()
        val cache = FakeConversationCache(stored = mutableMapOf(conversation.id to conversation))
        val useCase = GetConversationUseCase(repository, cache)

        val result = useCase(conversation.id)

        assertIs<GetConversationResult.Found>(result)
        assertEquals(conversation, result.conversation)
        assertEquals(0, repository.findCount)
        assertEquals(0, cache.putCount)
    }

    @Test
    fun `cache miss loads from the database and populates cache`() = runBlocking {
        val conversation = sampleConversation()
        val repository = FakeConversationRepository(byId = mutableMapOf(conversation.id to conversation))
        val cache = FakeConversationCache()
        val useCase = GetConversationUseCase(repository, cache)

        val result = useCase(conversation.id)

        assertIs<GetConversationResult.Found>(result)
        assertEquals(conversation, result.conversation)
        assertEquals(1, repository.findCount)
        assertEquals(conversation, cache.stored[conversation.id])
    }

    @Test
    fun `cache miss does not put when conversation is missing`() = runBlocking {
        val id = UUID.randomUUID()
        val cache = FakeConversationCache()
        val useCase = GetConversationUseCase(FakeConversationRepository(), cache)

        val result = useCase(id)

        assertIs<GetConversationResult.NotFound>(result)
        assertEquals(0, cache.putCount)
        assertTrue(cache.stored.isEmpty())
    }

    @Test
    fun `cache get failure still loads from the database`() = runBlocking {
        val conversation = sampleConversation()
        val repository = FakeConversationRepository(byId = mutableMapOf(conversation.id to conversation))
        val cache = FakeConversationCache(getError = IllegalStateException("redis unavailable"))
        val useCase = GetConversationUseCase(repository, cache)

        val result = useCase(conversation.id)

        assertIs<GetConversationResult.Found>(result)
        assertEquals(conversation, result.conversation)
        assertEquals(1, repository.findCount)
    }

    @Test
    fun `cache put failure still returns the database conversation`() = runBlocking {
        val conversation = sampleConversation()
        val repository = FakeConversationRepository(byId = mutableMapOf(conversation.id to conversation))
        val cache = FakeConversationCache(putError = IllegalStateException("redis unavailable"))
        val useCase = GetConversationUseCase(repository, cache)

        val result = useCase(conversation.id)

        assertIs<GetConversationResult.Found>(result)
        assertEquals(conversation, result.conversation)
        assertNull(cache.stored[conversation.id])
    }
}

class UpdateConversationUseCaseTest {

    @Test
    fun `update writes the database then invalidates cache`() = runBlocking {
        val conversation = sampleConversation()
        val repository = FakeConversationRepository(byId = mutableMapOf(conversation.id to conversation))
        val cache = FakeConversationCache(stored = mutableMapOf(conversation.id to conversation))
        val useCase = UpdateConversationUseCase(repository, cache)

        val result = useCase(UpdateConversationCommand(conversation.id, "  Renamed  "))

        assertIs<UpdateConversationResult.Updated>(result)
        assertEquals("Renamed", result.conversation.title)
        assertEquals("Renamed", repository.byId.getValue(conversation.id).title)
        assertEquals(listOf(conversation.id), cache.invalidated)
        assertNull(cache.stored[conversation.id])
    }

    @Test
    fun `missing conversation does not invalidate cache`() = runBlocking {
        val id = UUID.randomUUID()
        val cache = FakeConversationCache()
        val useCase = UpdateConversationUseCase(FakeConversationRepository(), cache)

        val result = useCase(UpdateConversationCommand(id, "Renamed"))

        assertIs<UpdateConversationResult.NotFound>(result)
        assertTrue(cache.invalidated.isEmpty())
        assertEquals(0, cache.invalidateCount)
    }

    @Test
    fun `invalid title does not touch the database or cache`() = runBlocking {
        val conversation = sampleConversation()
        val repository = FakeConversationRepository(byId = mutableMapOf(conversation.id to conversation))
        val cache = FakeConversationCache()
        val useCase = UpdateConversationUseCase(repository, cache)

        val result = useCase(UpdateConversationCommand(conversation.id, "   "))

        assertIs<UpdateConversationResult.InvalidTitle>(result)
        assertEquals("Existing", repository.byId.getValue(conversation.id).title)
        assertEquals(0, cache.invalidateCount)
    }

    @Test
    fun `cache invalidate failure still returns the updated conversation`() = runBlocking {
        val conversation = sampleConversation()
        val repository = FakeConversationRepository(byId = mutableMapOf(conversation.id to conversation))
        val cache = FakeConversationCache(invalidateError = IllegalStateException("redis unavailable"))
        val useCase = UpdateConversationUseCase(repository, cache)

        val result = useCase(UpdateConversationCommand(conversation.id, "Renamed"))

        assertIs<UpdateConversationResult.Updated>(result)
        assertEquals("Renamed", result.conversation.title)
    }
}

private fun sampleConversation() = Conversation(
    id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
    title = "Existing",
    createdAt = Instant.parse("2026-08-21T11:05:00Z"),
)

private class FakeConversationRepository(
    val byId: MutableMap<UUID, Conversation> = mutableMapOf(),
) : ConversationRepository {
    var findCount: Int = 0
        private set

    override suspend fun create(title: String, idempotencyKey: UUID): ConversationInsert {
        error("unused")
    }

    override suspend fun findById(id: UUID): Conversation? {
        findCount += 1
        return byId[id]
    }

    override suspend fun findAll(): List<Conversation> = byId.values.toList()

    override suspend fun update(id: UUID, title: String): Conversation? {
        val existing = byId[id] ?: return null
        val updated = existing.copy(title = title)
        byId[id] = updated
        return updated
    }
}

private class FakeConversationCache(
    val stored: MutableMap<UUID, Conversation> = mutableMapOf(),
    val invalidated: MutableList<UUID> = mutableListOf(),
    private val getError: Exception? = null,
    private val putError: Exception? = null,
    private val invalidateError: Exception? = null,
) : ConversationCache {
    var putCount: Int = 0
        private set
    var invalidateCount: Int = 0
        private set

    override suspend fun get(id: UUID): Conversation? {
        getError?.let { throw it }
        return stored[id]
    }

    override suspend fun put(conversation: Conversation) {
        putCount += 1
        putError?.let { throw it }
        stored[conversation.id] = conversation
    }

    override suspend fun invalidate(id: UUID) {
        invalidateCount += 1
        invalidateError?.let { throw it }
        stored.remove(id)
        invalidated += id
    }
}
