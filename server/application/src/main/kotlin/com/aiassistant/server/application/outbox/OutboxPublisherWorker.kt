package com.aiassistant.server.application.outbox

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.slf4j.LoggerFactory

class OutboxPublisherWorker(
    private val outboxPublisher: OutboxPublisher,
    private val intervalMs: Long = DEFAULT_INTERVAL_MS,
) {
    private val logger = LoggerFactory.getLogger(OutboxPublisherWorker::class.java)

    suspend fun run() {
        while (currentCoroutineContext().isActive) {
            try {
                outboxPublisher.publishBatch()
            } catch (error: Exception) {
                logger.warn("Outbox publish batch failed", error)
            }
            delay(intervalMs)
        }
    }

    private companion object {
        const val DEFAULT_INTERVAL_MS = 1_000L
    }
}
