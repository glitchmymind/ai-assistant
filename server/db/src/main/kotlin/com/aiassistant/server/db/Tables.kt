package com.aiassistant.server.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object Users : Table("users") {
    val id = uuid("id")
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}

object Conversations : Table("conversations") {
    val id = uuid("id")
    val userId = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}

object Messages : Table("messages") {
    val id = uuid("id")
    val conversationId = uuid("conversation_id")
        .references(Conversations.id, onDelete = ReferenceOption.CASCADE)
        .index("idx_messages_conversation_id")
    val role = varchar("role", 32)
    val content = text("content")
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}
