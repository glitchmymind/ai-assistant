package com.aiassistant.server.application.conversation.domain

import java.util.UUID

interface ConversationRepository {
    suspend fun create(title: String): Conversation
    suspend fun findById(id: UUID): Conversation?
}
