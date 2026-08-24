package com.aiassistant.server.application.conversation.cache

import com.aiassistant.server.application.conversation.domain.Conversation
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RedisConversationCacheTest {

    @Test
    fun `miss returns null`() = runBlocking {
        val store = FakeRedisStringStore()
        val cache = RedisConversationCache(store)
        val id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        assertNull(cache.get(id))
        assertEquals(listOf(RedisConversationCache.key(id)), store.gets)
    }

    @Test
    fun `put stores json with conversation v1 key and five minute ttl`() = runBlocking {
        val store = FakeRedisStringStore()
        val cache = RedisConversationCache(store)
        val conversation = sampleConversation()

        cache.put(conversation)
        val stored = cache.get(conversation.id)

        assertEquals(conversation, stored)
        assertEquals(RedisConversationCache.key(conversation.id), store.lastSet?.key)
        assertEquals(300L, store.lastSet?.ttlSeconds)
    }

    @Test
    fun `invalidate deletes the cache key`() = runBlocking {
        val store = FakeRedisStringStore()
        val cache = RedisConversationCache(store)
        val conversation = sampleConversation()

        cache.put(conversation)
        cache.invalidate(conversation.id)

        assertNull(cache.get(conversation.id))
        assertEquals(listOf(RedisConversationCache.key(conversation.id)), store.deleted)
    }

    @Test
    fun `get returns null when redis fails`() = runBlocking {
        val cache = RedisConversationCache(FakeRedisStringStore(failGet = true))
        assertNull(cache.get(sampleConversation().id))
    }

    @Test
    fun `put swallows redis failures`() = runBlocking {
        val cache = RedisConversationCache(FakeRedisStringStore(failSet = true))
        cache.put(sampleConversation())
    }

    @Test
    fun `invalidate swallows redis failures`() = runBlocking {
        val cache = RedisConversationCache(FakeRedisStringStore(failDel = true))
        cache.invalidate(sampleConversation().id)
    }

    @Test
    fun `corrupt payload is treated as a cache error`() = runBlocking {
        val conversation = sampleConversation()
        val key = RedisConversationCache.key(conversation.id)
        val store = FakeRedisStringStore(values = mutableMapOf(key to "{not-json"))
        val cache = RedisConversationCache(store)

        assertNull(cache.get(conversation.id))
    }
}

private fun sampleConversation() = Conversation(
    id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
    title = "Planning",
    createdAt = Instant.parse("2026-08-21T11:05:00Z"),
)

private class FakeRedisStringStore(
    private val values: MutableMap<String, String> = mutableMapOf(),
    private val failGet: Boolean = false,
    private val failSet: Boolean = false,
    private val failDel: Boolean = false,
) : RedisStringStore {
    val gets = mutableListOf<String>()
    val deleted = mutableListOf<String>()
    var lastSet: SetCall? = null
        private set

    override suspend fun get(key: String): String? {
        gets += key
        if (failGet) error("redis unavailable")
        return values[key]
    }

    override suspend fun setex(key: String, ttlSeconds: Long, value: String) {
        lastSet = SetCall(key, ttlSeconds, value)
        if (failSet) error("redis unavailable")
        values[key] = value
    }

    override suspend fun del(key: String) {
        deleted += key
        if (failDel) error("redis unavailable")
        values.remove(key)
    }
}

private data class SetCall(
    val key: String,
    val ttlSeconds: Long,
    val value: String,
)
