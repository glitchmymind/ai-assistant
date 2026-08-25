package com.aiassistant.server.application.outbox

import com.aiassistant.server.core.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun interface KafkaMessageSender {
    suspend fun send(topic: String, key: String, value: String)
}

class KafkaProducerSender(
    private val producer: KafkaProducer<String, String>,
) : KafkaMessageSender, AutoCloseable {
    override suspend fun send(topic: String, key: String, value: String) {
        val record = ProducerRecord(topic, key, value)
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val future = producer.send(record) { _, exception ->
                    when {
                        exception != null -> continuation.resumeWithException(exception)
                        else -> continuation.resume(Unit)
                    }
                }
                continuation.invokeOnCancellation {
                    future.cancel(true)
                }
            }
        }
    }

    override fun close() {
        producer.close(Duration.ofSeconds(5))
    }
}

object UnavailableKafkaMessageSender : KafkaMessageSender {
    override suspend fun send(topic: String, key: String, value: String) {
        error("Kafka is unavailable")
    }
}

object KafkaFactory {
    private val logger = LoggerFactory.getLogger(KafkaFactory::class.java)

    fun createSender(): KafkaMessageSender {
        return try {
            val properties = Properties().apply {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.kafkaBootstrapServers)
                put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
                put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
                put(ProducerConfig.ACKS_CONFIG, "all")
                put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "false")
                put(ProducerConfig.RETRIES_CONFIG, 5)
                put(ProducerConfig.CLIENT_ID_CONFIG, "ai-assistant-outbox")
            }
            KafkaProducerSender(KafkaProducer(properties))
        } catch (error: Exception) {
            logger.warn("Kafka producer could not be created: ${error.message}")
            UnavailableKafkaMessageSender
        }
    }
}
