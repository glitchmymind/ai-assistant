package com.aiassistant.features.home.presentation

import com.aiassistant.common.core.UiState
import com.aiassistant.features.conversation.domain.model.Conversation
import com.aiassistant.features.conversation.presentation.model.mvi.ConversationUiState

data class HomeUiState(
    val health: HealthUiState,
    val title: String,
    val conversationId: String,
    val conversation: ConversationUiState,
    val conversations: List<Conversation> = emptyList(),
    val conversationsLoaded: Boolean = false,
) : UiState
