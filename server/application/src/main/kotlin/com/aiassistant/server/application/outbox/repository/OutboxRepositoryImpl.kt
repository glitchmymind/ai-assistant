package com.aiassistant.server.application.outbox.repository

import com.aiassistant.server.application.outbox.DomainEvent
import com.aiassistant.server.application.outbox.NewDomainEvent
import com.aiassistant.server.application.outbox.OutboxRepository
import com.aiassistant.server.db.OutboxEventRow
import com.aiassistant.server.db.OutboxEvents
import com.aiassistant.server.db.toOutboxEventRow
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class OutboxRepositoryImpl : OutboxRepository {

    override suspend fun insert(event: NewDomainEvent) {
        OutboxEvents.insert {
            it[aggregateType] = event.aggregateType
            it[aggregateId] = event.aggregateId
            it[eventType] = event.eventType
            it[eventVersion] = event.eventVersion
            it[payload] = event.payload
        }
    }

    override suspend fun findUnpublished(limit: Int): List<DomainEvent> = dbQuery {
        OutboxEvents
            .selectAll()
            .where { OutboxEvents.publishedAt.isNull() }
            .orderBy(OutboxEvents.createdAt to SortOrder.ASC)
            .limit(limit)
            .map { it.toOutboxEventRow().toDomain() }
    }

    override suspend fun markPublished(id: UUID) {
        dbQuery {
            OutboxEvents.update({ OutboxEvents.id eq id }) {
                it[publishedAt] = OffsetDateTime.now(ZoneOffset.UTC)
            }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(context = Dispatchers.IO) { block() }
}

private fun OutboxEventRow.toDomain() = DomainEvent(
    id = id,
    aggregateType = aggregateType,
    aggregateId = aggregateId,
    eventType = eventType,
    eventVersion = eventVersion,
    payload = payload,
    createdAt = createdAt.toInstant(),
)
