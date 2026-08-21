package com.aiassistant.features.conversation.domain

import com.aiassistant.features.conversation.domain.model.Conversation
import com.aiassistant.features.conversation.domain.model.ConversationResult

interface ConversationRepository {
    suspend fun create(title: String): ConversationResult<Conversation>
    suspend fun get(id: String): ConversationResult<Conversation>
}
