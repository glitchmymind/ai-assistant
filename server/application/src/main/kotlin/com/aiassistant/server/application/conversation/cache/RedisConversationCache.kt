package com.aiassistant.server.application.conversation.cache

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationCache
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

class RedisConversationCache(
    private val redis: RedisStringStore,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : ConversationCache {
    private val logger = LoggerFactory.getLogger(RedisConversationCache::class.java)

    override suspend fun get(id: UUID): Conversation? {
        val key = key(id)
        return try {
            val payload = redis.get(key)
            if (payload == null) {
                logger.info("cache miss key={}", key)
                null
            } else {
                logger.info("cache hit key={}", key)
                json.decodeFromString<CachedConversation>(payload).toDomain()
            }
        } catch (error: Exception) {
            logger.warn("cache error key={} op=get", key, error)
            null
        }
    }

    override suspend fun put(conversation: Conversation) {
        val key = key(conversation.id)
        try {
            redis.setex(
                key = key,
                ttlSeconds = TTL_SECONDS,
                value = json.encodeToString(CachedConversation.serializer(), conversation.toCached()),
            )
        } catch (error: Exception) {
            logger.warn("cache error key={} op=put", key, error)
        }
    }

    override suspend fun invalidate(id: UUID) {
        val key = key(id)
        try {
            redis.del(key)
        } catch (error: Exception) {
            logger.warn("cache error key={} op=invalidate", key, error)
        }
    }

    companion object {
        const val KEY_PREFIX = "conversation:v1:"
        const val TTL_SECONDS = 300L

        fun key(id: UUID): String = "$KEY_PREFIX$id"
    }
}

@Serializable
internal data class CachedConversation(
    val id: String,
    val title: String,
    val createdAt: String,
)

private fun Conversation.toCached() = CachedConversation(
    id = id.toString(),
    title = title,
    createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt),
)

private fun CachedConversation.toDomain() = Conversation(
    id = UUID.fromString(id),
    title = title,
    createdAt = Instant.parse(createdAt),
)
