package com.aiassistant.features.home.presentation

import androidx.compose.runtime.Composable
import com.aiassistant.common.core.UiState

sealed interface HealthUiState : UiState {

    data object Loading : HealthUiState

    data class Available(
        val version: String,
    ) : HealthUiState

    data object Unavailable : HealthUiState
}

@Composable
fun HealthUiState.onLoading(block: @Composable () -> Unit): HealthUiState {
    if (this is HealthUiState.Loading) {
        block()
    }
    Result
    return this
}

@Composable
fun HealthUiState.onFailure(block: @Composable () -> Unit): HealthUiState {
    if (this is HealthUiState.Unavailable) {
        block()
    }
    return this
}

@Composable
fun HealthUiState.onSuccess(block: @Composable (HealthUiState.Available) -> Unit): HealthUiState {
    if (this is HealthUiState.Available) {
        block(this)
    }
    return this
}
