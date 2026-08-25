package com.aiassistant.server.application.outbox

class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val eventPublisher: EventPublisher,
) {
    suspend fun publishBatch() {
        val events = outboxRepository.findUnpublished(limit = BATCH_LIMIT)

        for (event in events) {
            eventPublisher.publish(event)
            outboxRepository.markPublished(event.id)
        }
    }

    private companion object {
        const val BATCH_LIMIT = 100
    }
}
