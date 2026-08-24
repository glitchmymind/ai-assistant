package com.aiassistant.server.application.conversation

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationCache
import com.aiassistant.server.application.conversation.domain.ConversationRepository
import org.slf4j.LoggerFactory
import java.util.UUID

class GetConversationUseCase(
    private val conversationRepository: ConversationRepository,
    private val conversationCache: ConversationCache,
) {
    private val logger = LoggerFactory.getLogger(GetConversationUseCase::class.java)

    suspend operator fun invoke(id: UUID): GetConversationResult {
        try {
            conversationCache.get(id)?.let {
                return GetConversationResult.Found(it)
            }
        } catch (error: Exception) {
            logger.warn("cache error id={} op=get", id, error)
        }

        val conversation = conversationRepository.findById(id)
            ?: return GetConversationResult.NotFound

        try {
            conversationCache.put(conversation)
        } catch (error: Exception) {
            logger.warn("cache error id={} op=put", id, error)
        }
        return GetConversationResult.Found(conversation)
    }
}

sealed interface GetConversationResult {
    data class Found(val conversation: Conversation) : GetConversationResult
    data object NotFound : GetConversationResult
}
