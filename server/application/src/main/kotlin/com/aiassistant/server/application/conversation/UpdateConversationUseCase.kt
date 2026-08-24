package com.aiassistant.server.application.conversation

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationCache
import com.aiassistant.server.application.conversation.domain.ConversationRepository
import org.slf4j.LoggerFactory

class UpdateConversationUseCase(
    private val conversationRepository: ConversationRepository,
    private val conversationCache: ConversationCache,
) {
    private val logger = LoggerFactory.getLogger(UpdateConversationUseCase::class.java)

    suspend operator fun invoke(command: UpdateConversationCommand): UpdateConversationResult {
        val normalizedTitle = command.title.trim()
        if (normalizedTitle.isEmpty() || normalizedTitle.length > MAX_TITLE_LENGTH) {
            return UpdateConversationResult.InvalidTitle
        }
        val updated = conversationRepository.update(command.id, normalizedTitle)
            ?: return UpdateConversationResult.NotFound

        try {
            conversationCache.invalidate(command.id)
        } catch (error: Exception) {
            logger.warn("cache error id={} op=invalidate", command.id, error)
        }
        return UpdateConversationResult.Updated(updated)
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 200
    }
}

sealed interface UpdateConversationResult {

    data class Updated(val conversation: Conversation) : UpdateConversationResult

    data object NotFound : UpdateConversationResult

    data object InvalidTitle : UpdateConversationResult
}
