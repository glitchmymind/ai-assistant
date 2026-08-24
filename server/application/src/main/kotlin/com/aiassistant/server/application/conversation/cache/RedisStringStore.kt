package com.aiassistant.server.application.conversation.cache

import com.aiassistant.server.core.AppConfig
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

interface RedisStringStore {

    suspend fun get(key: String): String?

    suspend fun setex(key: String, ttlSeconds: Long, value: String)

    suspend fun del(key: String)
}

class LettuceRedisStringStore(
    private val connection: StatefulRedisConnection<String, String>,
) : RedisStringStore {
    override suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        connection.sync().get(key)
    }

    override suspend fun setex(key: String, ttlSeconds: Long, value: String) {
        withContext(Dispatchers.IO) {
            connection.sync().setex(key, ttlSeconds, value)
        }
    }

    override suspend fun del(key: String) {
        withContext(Dispatchers.IO) {
            connection.sync().del(key)
        }
    }
}

object NoOpRedisStringStore : RedisStringStore {
    override suspend fun get(key: String): String? = null
    override suspend fun setex(key: String, ttlSeconds: Long, value: String) = Unit
    override suspend fun del(key: String) = Unit
}

object RedisFactory {
    private val logger = LoggerFactory.getLogger(RedisFactory::class.java)

    fun createStore(): RedisStringStore {
        return try {
            val client = RedisClient.create(
                RedisURI.create("redis://${AppConfig.redisHost}:${AppConfig.redisPort}"),
            )
            LettuceRedisStringStore(client.connect())
        } catch (error: Exception) {
            logger.warn("Redis is unavailable: ${error.message}")
            NoOpRedisStringStore
        }
    }
}
