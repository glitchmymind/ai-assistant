package com.aiassistant.server.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object Users : Table("users") {
    val id = uuid("id").databaseGenerated()
    val createdAt = timestampWithTimeZone("created_at").databaseGenerated()

    override val primaryKey = PrimaryKey(id)
}

object Conversations : Table("conversations") {
    val id = uuid("id").databaseGenerated()
    val title = varchar("title", 200)
    val createdAt = timestampWithTimeZone("created_at").databaseGenerated()

    override val primaryKey = PrimaryKey(id)
}

object Messages : Table("messages") {
    val id = uuid("id").databaseGenerated()
    val conversationId = uuid("conversation_id")
        .references(Conversations.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_messages_conversation_id")
    val role = varchar("role", 32)
    val content = text("content")
    val createdAt = timestampWithTimeZone("created_at").databaseGenerated()

    override val primaryKey = PrimaryKey(id)
}
