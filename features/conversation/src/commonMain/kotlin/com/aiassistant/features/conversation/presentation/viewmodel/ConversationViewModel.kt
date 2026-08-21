package com.aiassistant.features.conversation.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.aiassistant.common.core.MviViewModel
import com.aiassistant.features.conversation.domain.ConversationRepository
import com.aiassistant.features.conversation.domain.model.Conversation
import com.aiassistant.features.conversation.domain.model.ConversationResult
import com.aiassistant.features.conversation.presentation.model.mvi.ConversationUiAction
import com.aiassistant.features.conversation.presentation.model.mvi.ConversationUiState
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val conversationRepository: ConversationRepository,
) : MviViewModel<ConversationUiState, ConversationUiAction>() {

    override fun initState(): ConversationUiState = ConversationUiState.Idle

    override fun onAction(action: ConversationUiAction) {
        when (action) {
            is ConversationUiAction.Create -> create(action.title)
            is ConversationUiAction.Load -> load(action.id)
        }
    }

    private fun create(title: String) {
        viewModelScope.launch {
            updateState { ConversationUiState.Loading }
            val result = conversationRepository.create(title)
            updateState { result.toUiState() }
        }
    }

    private fun load(id: String) {
        viewModelScope.launch {
            updateState { ConversationUiState.Loading }
            val result = conversationRepository.get(id)
            updateState { result.toUiState() }
        }
    }

    private fun ConversationResult<Conversation>.toUiState(): ConversationUiState {
        return when (this) {
            is ConversationResult.Success -> ConversationUiState.Loaded(value)
            is ConversationResult.Failure -> ConversationUiState.Failed(error)
        }
    }
}
