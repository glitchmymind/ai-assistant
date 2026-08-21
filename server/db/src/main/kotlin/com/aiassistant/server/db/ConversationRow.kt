package com.aiassistant.server.db

import org.jetbrains.exposed.sql.ResultRow
import java.time.OffsetDateTime
import java.util.UUID

data class ConversationRow(
    val id: UUID,
    val title: String,
    val createdAt: OffsetDateTime,
)

fun ResultRow.toConversationRow(): ConversationRow = ConversationRow(
    id = this[Conversations.id],
    title = this[Conversations.title],
    createdAt = this[Conversations.createdAt],
)
