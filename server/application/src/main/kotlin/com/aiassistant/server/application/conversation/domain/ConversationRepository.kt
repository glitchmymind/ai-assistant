package com.aiassistant.server.application.conversation.domain

import java.util.UUID

interface ConversationRepository {

    suspend fun create(title: String, idempotencyKey: UUID): ConversationInsert

    suspend fun findById(id: UUID): Conversation?

    suspend fun findAll(): List<Conversation>

    suspend fun update(id: UUID, title: String): Conversation?
}

sealed interface ConversationInsert {
    val conversation: Conversation

    data class Inserted(override val conversation: Conversation) : ConversationInsert

    data class AlreadyExists(override val conversation: Conversation) : ConversationInsert
}
