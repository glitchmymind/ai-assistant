package com.aiassistant.features.home.presentation

import com.aiassistant.common.core.UiState
import com.aiassistant.features.conversation.presentation.model.mvi.ConversationUiState

data class HomeUiState(
    val health: HealthUiState,
    val title: String,
    val conversationId: String,
    val conversation: ConversationUiState,
) : UiState
