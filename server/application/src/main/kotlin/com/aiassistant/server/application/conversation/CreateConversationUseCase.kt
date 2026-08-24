package com.aiassistant.server.application.conversation

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationInsert
import com.aiassistant.server.application.conversation.domain.ConversationRepository

class CreateConversationUseCase(
    private val conversationRepository: ConversationRepository,
) {
    suspend operator fun invoke(command: CreateConversationCommand): CreateConversationResult {
        val normalizedTitle = command.title.trim()
        if (normalizedTitle.isEmpty() || normalizedTitle.length > MAX_TITLE_LENGTH) {
            return CreateConversationResult.InvalidTitle
        }
        return when (
            val insert = conversationRepository.create(normalizedTitle, command.idempotencyKey)
        ) {
            is ConversationInsert.Inserted -> CreateConversationResult.Created(insert.conversation)
            is ConversationInsert.AlreadyExists -> CreateConversationResult.Existing(insert.conversation)
        }
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 200
    }
}

sealed interface CreateConversationResult {
    data class Created(val conversation: Conversation) : CreateConversationResult
    data class Existing(val conversation: Conversation) : CreateConversationResult
    data object InvalidTitle : CreateConversationResult
}
