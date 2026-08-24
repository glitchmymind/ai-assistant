package com.aiassistant.server.application.conversation

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationRepository

class ListConversationsUseCase(
    private val conversationRepository: ConversationRepository,
) {
    suspend operator fun invoke(): List<Conversation> = conversationRepository.findAll()
}
