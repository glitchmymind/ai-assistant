package com.aiassistant.server.application.conversation

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationRepository
import java.util.UUID

class GetConversationUseCase(
    private val conversationRepository: ConversationRepository,
) {
    suspend operator fun invoke(id: UUID): GetConversationResult {
        val conversation = conversationRepository.findById(id)
            ?: return GetConversationResult.NotFound
        return GetConversationResult.Found(conversation)
    }
}

sealed interface GetConversationResult {
    data class Found(val conversation: Conversation) : GetConversationResult
    data object NotFound : GetConversationResult
}
