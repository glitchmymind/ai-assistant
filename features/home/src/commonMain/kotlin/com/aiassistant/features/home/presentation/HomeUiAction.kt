package com.aiassistant.features.home.presentation

import com.aiassistant.common.core.UiAction

sealed interface HomeUiAction : UiAction {
    data object Refresh : HomeUiAction
    data class TitleChanged(val value: String) : HomeUiAction
    data class ConversationIdChanged(val value: String) : HomeUiAction
    data object CreateConversation : HomeUiAction
    data object LoadConversation : HomeUiAction
    data object LoadAllConversations : HomeUiAction
    data object UpdateConversation : HomeUiAction
}
