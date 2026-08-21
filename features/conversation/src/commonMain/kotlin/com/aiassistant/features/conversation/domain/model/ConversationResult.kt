package com.aiassistant.features.conversation.domain.model

sealed interface ConversationError {
    data object MalformedId : ConversationError
    data object NotFound : ConversationError
    data object InvalidTitle : ConversationError
    data object Server : ConversationError
    data object Network : ConversationError
}

sealed interface ConversationResult<out T> {
    data class Success<T>(val value: T) : ConversationResult<T>
    data class Failure(val error: ConversationError) : ConversationResult<Nothing>
}
