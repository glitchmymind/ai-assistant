package com.aiassistant.features.home.presentation

import androidx.lifecycle.viewModelScope
import com.aiassistant.common.core.MviViewModel
import com.aiassistant.features.conversation.domain.ConversationRepository
import com.aiassistant.features.conversation.domain.model.ConversationResult
import com.aiassistant.features.conversation.presentation.model.mvi.ConversationUiState
import com.aiassistant.features.home.domain.CheckHealthUseCase
import com.aiassistant.features.home.domain.SystemHealthResult
import kotlinx.coroutines.launch
import kotlin.String

class HomeViewModel(
    private val checkHealthUseCase: CheckHealthUseCase,
    private val conversationRepository: ConversationRepository,
) : MviViewModel<HomeUiState, HomeUiAction>() {

    init {
        onAction(HomeUiAction.Refresh)
    }

    override fun initState(): HomeUiState = HomeUiState(
        health = HealthUiState.Loading,
        title = "",
        conversationId = "",
        conversation = ConversationUiState.Idle,
    )

    override fun onAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.Refresh -> refresh()
            is HomeUiAction.TitleChanged -> updateState { copy(title = action.value) }
            is HomeUiAction.ConversationIdChanged -> updateState { copy(conversationId = action.value) }
            HomeUiAction.CreateConversation -> createConversation()
            HomeUiAction.LoadConversation -> loadConversation()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            updateState { copy(health = HealthUiState.Loading) }
            val health = runCatching { checkHealthUseCase() }
                .getOrDefault(SystemHealthResult.Unavailable)
            updateState {
                copy(
                    health = when (health) {
                        is SystemHealthResult.Healthy -> HealthUiState.Available(health.version)
                        SystemHealthResult.Unavailable -> HealthUiState.Unavailable
                    },
                )
            }
        }
    }

    private fun createConversation() {
        val title = uiStateValue.title
        viewModelScope.launch {
            updateState { copy(conversation = ConversationUiState.Loading) }
            when (val result = conversationRepository.create(title)) {
                is ConversationResult.Success -> updateState {
                    copy(
                        conversation = ConversationUiState.Loaded(result.value),
                        title = result.value.title,
                        conversationId = result.value.id,
                    )
                }

                is ConversationResult.Failure -> updateState {
                    copy(conversation = ConversationUiState.Failed(result.error))
                }
            }
        }
    }

    private fun loadConversation() {
        val id = uiStateValue.conversationId.trim()
        viewModelScope.launch {
            updateState { copy(conversation = ConversationUiState.Loading) }
            when (val result = conversationRepository.get(id)) {
                is ConversationResult.Success -> updateState {
                    copy(
                        conversation = ConversationUiState.Loaded(result.value),
                        title = result.value.title,
                        conversationId = result.value.id,
                    )
                }

                is ConversationResult.Failure -> updateState {
                    copy(conversation = ConversationUiState.Failed(result.error))
                }
            }
        }
    }
}
