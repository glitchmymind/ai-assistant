package com.aiassistant.features.home.data

import com.aiassistant.common.core.SystemHealth
import com.aiassistant.features.home.data.local.SystemLocalDataSource
import com.aiassistant.features.home.data.remote.SystemRemoteDataSource
import com.aiassistant.features.home.domain.SystemRepository

class SystemRepositoryImpl(
    private val remoteDataSource: SystemRemoteDataSource,
    private val localDataSource: SystemLocalDataSource,
) : SystemRepository {
    override suspend fun checkHealth(): SystemHealth {
        return try {
            remoteDataSource.checkHealth().also { health ->
                localDataSource.saveHealth(health)
            }
        } catch (_: Exception) {
            localDataSource.getHealth()
                ?: SystemHealth(isAvailable = false, version = "unknown")
        }
    }
}
