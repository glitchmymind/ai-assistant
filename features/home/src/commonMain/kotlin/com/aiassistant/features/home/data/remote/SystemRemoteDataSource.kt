package com.aiassistant.features.home.data.remote

import com.aiassistant.features.home.data.mapper.SystemDataMapper.toDomain
import com.aiassistant.features.home.domain.SystemHealthResult

interface SystemRemoteDataSource {
    suspend fun checkHealth(): SystemHealthResult
}

class SystemRemoteDataSourceImpl(
    private val systemApi: SystemApi,
) : SystemRemoteDataSource {

    override suspend fun checkHealth(): SystemHealthResult {
        return systemApi.checkHealth().toDomain()
    }
}
