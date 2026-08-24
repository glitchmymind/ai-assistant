package com.aiassistant.server.application.conversation.domain

import java.util.UUID

interface ConversationCache {

    suspend fun get(id: UUID): Conversation?

    suspend fun put(conversation: Conversation)

    suspend fun invalidate(id: UUID)
}
