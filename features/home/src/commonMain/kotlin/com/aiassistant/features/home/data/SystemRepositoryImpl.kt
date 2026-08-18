package com.aiassistant.features.home.data

import com.aiassistant.features.home.data.local.SystemLocalDataSource
import com.aiassistant.features.home.data.remote.SystemRemoteDataSource
import com.aiassistant.features.home.domain.SystemHealthResult
import com.aiassistant.features.home.domain.SystemRepository

class SystemRepositoryImpl(
    private val remoteDataSource: SystemRemoteDataSource,
    private val localDataSource: SystemLocalDataSource,
) : SystemRepository {
    override suspend fun checkHealth(): SystemHealthResult {
        return try {
            when (val result = remoteDataSource.checkHealth()) {
                is SystemHealthResult.Healthy -> {
                    localDataSource.saveHealth(result)
                    result
                }
                SystemHealthResult.Unavailable -> SystemHealthResult.Unavailable
            }
        } catch (_: Exception) {
            SystemHealthResult.Unavailable
        }
    }
}
