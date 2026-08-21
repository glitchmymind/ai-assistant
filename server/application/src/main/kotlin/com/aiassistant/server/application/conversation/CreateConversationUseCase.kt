package com.aiassistant.server.application.conversation

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationRepository

class CreateConversationUseCase(
    private val conversationRepository: ConversationRepository,
) {
    suspend operator fun invoke(title: String): CreateConversationResult {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty() || normalizedTitle.length > MAX_TITLE_LENGTH) {
            return CreateConversationResult.InvalidTitle
        }
        return CreateConversationResult.Created(
            conversationRepository.create(normalizedTitle),
        )
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 200
    }
}

sealed interface CreateConversationResult {
    data class Created(val conversation: Conversation) : CreateConversationResult
    data object InvalidTitle : CreateConversationResult
}
