package com.aiassistant.common.network

import com.aiassistant.common.core.AppEnvironment
import com.aiassistant.common.core.apiBaseUrl

object NetworkConfig {
    const val HEALTH_PATH = "/api/v1/health"
}

fun defaultBaseUrl(
    environment: AppEnvironment = AppEnvironment.current,
): String = environment.apiBaseUrl(localLoopbackHost())

expect fun localLoopbackHost(): String
