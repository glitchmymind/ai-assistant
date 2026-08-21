package com.aiassistant.features.conversation.presentation.model.mvi

import com.aiassistant.common.core.UiState
import com.aiassistant.features.conversation.domain.model.Conversation
import com.aiassistant.features.conversation.domain.model.ConversationError

sealed interface ConversationUiState : UiState {
    data object Idle : ConversationUiState
    data object Loading : ConversationUiState
    data class Loaded(val conversation: Conversation) : ConversationUiState
    data class Failed(val error: ConversationError) : ConversationUiState
}
