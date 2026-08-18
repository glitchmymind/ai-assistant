package com.aiassistant.common.core

enum class AppEnvironment {
    LOCAL,
    STAGING,
    PROD;

    companion object {
        val current: AppEnvironment = LOCAL
    }
}

fun AppEnvironment.apiBaseUrl(localHost: String): String {
    return when (this) {
        AppEnvironment.LOCAL -> "http://$localHost:8080"
        AppEnvironment.STAGING -> "https://staging-api.aiassistant.app"
        AppEnvironment.PROD -> "https://api.aiassistant.app"
    }
}
