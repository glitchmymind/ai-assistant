package com.aiassistant.server.db

import org.jetbrains.exposed.sql.ResultRow
import java.time.OffsetDateTime
import java.util.UUID

data class OutboxEventRow(
    val id: UUID,
    val aggregateType: String,
    val aggregateId: UUID,
    val eventType: String,
    val eventVersion: Int,
    val payload: String,
    val createdAt: OffsetDateTime,
    val publishedAt: OffsetDateTime?,
)

fun ResultRow.toOutboxEventRow(): OutboxEventRow = OutboxEventRow(
    id = this[OutboxEvents.id],
    aggregateType = this[OutboxEvents.aggregateType],
    aggregateId = this[OutboxEvents.aggregateId],
    eventType = this[OutboxEvents.eventType],
    eventVersion = this[OutboxEvents.eventVersion],
    payload = this[OutboxEvents.payload],
    createdAt = this[OutboxEvents.createdAt],
    publishedAt = this[OutboxEvents.publishedAt],
)
