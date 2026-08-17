package com.aiassistant.features.home.data.local

import com.aiassistant.common.core.SystemHealth

interface SystemLocalDataSource {
    suspend fun saveHealth(health: SystemHealth)
    suspend fun getHealth(): SystemHealth?
}

class SystemLocalDataSourceImpl : SystemLocalDataSource {
    private var cachedHealth: SystemHealth? = null

    override suspend fun saveHealth(health: SystemHealth) {
        cachedHealth = health
    }

    override suspend fun getHealth(): SystemHealth? = cachedHealth
}
