package com.aiassistant.common.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface UiState
interface UiAction

abstract class MviViewModel<State : UiState, Action : UiAction> : ViewModel() {

    private val _uiState: MutableStateFlow<State> by lazy { MutableStateFlow(initState()) }
    val uiState: StateFlow<State> by lazy { _uiState.asStateFlow() }

    val uiStateValue: State
        get() = uiState.value

    abstract fun initState(): State

    abstract fun onAction(action: Action)

    fun updateState(modify: State.() -> State) {
        _uiState.update { it.modify() }
    }
}
