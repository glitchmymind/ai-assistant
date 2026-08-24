package com.aiassistant.common.network

import com.aiassistant.common.core.AppEnvironment
import com.aiassistant.common.core.apiBaseUrl

object NetworkConfig {
    const val HEALTH_PATH = "/api/v1/health"
    const val CONVERSATIONS_PATH = "/api/v1/conversations"
    const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"

    fun conversationPath(id: String): String = "$CONVERSATIONS_PATH/$id"
}

fun defaultBaseUrl(
    environment: AppEnvironment = AppEnvironment.current,
): String = environment.apiBaseUrl(localLoopbackHost())

expect fun localLoopbackHost(): String
