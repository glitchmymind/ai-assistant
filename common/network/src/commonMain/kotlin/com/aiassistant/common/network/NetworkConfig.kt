package com.aiassistant.common.network

object NetworkConfig {
    const val HEALTH_PATH = "/health"
}

expect fun defaultBaseUrl(): String
