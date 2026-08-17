package com.aiassistant

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.aiassistant.common.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AI Assistant",
        ) {
            App()
        }
    }
}
