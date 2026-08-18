package com.aiassistant.features.home.presentation

import com.aiassistant.common.core.SystemHealth
import com.aiassistant.common.core.UiState

data class HomeUiState(
    val isLoading: Boolean = false,
    val health: SystemHealth? = null,
    val errorMessage: String? = null,
) : UiState
