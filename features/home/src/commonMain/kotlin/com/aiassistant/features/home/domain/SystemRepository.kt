package com.aiassistant.features.home.domain

interface SystemRepository {
    suspend fun checkHealth(): SystemHealthResult
}
