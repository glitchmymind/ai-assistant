package com.aiassistant.features.home.presentation

import androidx.lifecycle.viewModelScope
import com.aiassistant.common.core.MviViewModel
import com.aiassistant.features.home.domain.CheckHealthUseCase
import com.aiassistant.features.home.domain.SystemHealthResult
import kotlinx.coroutines.launch

class HomeViewModel(
    private val checkHealthUseCase: CheckHealthUseCase,
) : MviViewModel<HealthUiState, HomeUiAction>() {

    init {
        onAction(HomeUiAction.Refresh)
    }

    override fun initState(): HealthUiState = HealthUiState.Loading

    override fun onAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            updateState { HealthUiState.Loading }
            val health = runCatching { checkHealthUseCase() }
                .getOrDefault(SystemHealthResult.Unavailable)
            updateState {
                when (health) {
                    is SystemHealthResult.Healthy -> HealthUiState.Available(health.version)
                    SystemHealthResult.Unavailable -> HealthUiState.Unavailable
                }
            }
        }
    }
}
