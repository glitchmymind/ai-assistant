package com.aiassistant.features.home.domain

import com.aiassistant.common.core.SystemHealth

interface SystemRepository {
    suspend fun checkHealth(): SystemHealth
}
