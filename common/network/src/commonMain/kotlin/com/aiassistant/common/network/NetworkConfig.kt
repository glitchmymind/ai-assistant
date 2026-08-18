package com.aiassistant.common.network

object NetworkConfig {
    const val HEALTH_PATH = "/api/v1/health"
}

expect fun defaultBaseUrl(): String
