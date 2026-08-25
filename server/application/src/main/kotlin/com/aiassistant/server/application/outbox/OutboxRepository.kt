package com.aiassistant.server.application.outbox

import java.util.UUID

interface OutboxRepository {
    suspend fun insert(event: NewDomainEvent)

    suspend fun findUnpublished(limit: Int): List<DomainEvent>

    suspend fun markPublished(id: UUID)
}
