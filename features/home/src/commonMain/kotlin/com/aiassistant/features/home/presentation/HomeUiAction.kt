package com.aiassistant.features.home.presentation

import com.aiassistant.common.core.UiAction

sealed interface HomeUiAction : UiAction {
    data object Refresh : HomeUiAction
}
