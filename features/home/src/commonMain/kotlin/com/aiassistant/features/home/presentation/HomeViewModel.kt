package com.aiassistant.features.home.presentation

import androidx.lifecycle.viewModelScope
import com.aiassistant.common.core.MviViewModel
import com.aiassistant.features.home.domain.CheckHealthUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val checkHealthUseCase: CheckHealthUseCase,
) : MviViewModel<HomeUiState, HomeUiAction>() {

    init {
        refresh()
    }

    override fun initState(): HomeUiState = HomeUiState()

    override fun onAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            runCatching { checkHealthUseCase() }
                .onSuccess { health ->
                    updateState {
                        copy(
                            isLoading = false,
                            health = health,
                            errorMessage = if (health.isAvailable) null else "API is unavailable",
                        )
                    }
                }
                .onFailure { error ->
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to check health",
                        )
                    }
                }
        }
    }
}
