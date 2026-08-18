package com.aiassistant.features.home.data.local

import com.aiassistant.features.home.domain.SystemHealthResult

interface SystemLocalDataSource {
    suspend fun saveHealth(health: SystemHealthResult.Healthy)
    suspend fun getHealth(): SystemHealthResult.Healthy?
}

class SystemLocalDataSourceImpl : SystemLocalDataSource {
    private var cachedHealth: SystemHealthResult.Healthy? = null

    override suspend fun saveHealth(health: SystemHealthResult.Healthy) {
        cachedHealth = health
    }

    override suspend fun getHealth(): SystemHealthResult.Healthy? = cachedHealth
}
