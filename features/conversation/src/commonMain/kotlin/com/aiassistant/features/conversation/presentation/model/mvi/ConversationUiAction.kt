package com.aiassistant.features.conversation.presentation.model.mvi

import com.aiassistant.common.core.UiAction

sealed interface ConversationUiAction : UiAction {
    data class Create(val title: String) : ConversationUiAction
    data class Load(val id: String) : ConversationUiAction
}
