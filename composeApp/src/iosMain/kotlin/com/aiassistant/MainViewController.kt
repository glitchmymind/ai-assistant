package com.aiassistant

import androidx.compose.ui.window.ComposeUIViewController
import com.aiassistant.common.di.initKoin

fun MainViewController() = run {
    initKoin()
    ComposeUIViewController { App() }
}
