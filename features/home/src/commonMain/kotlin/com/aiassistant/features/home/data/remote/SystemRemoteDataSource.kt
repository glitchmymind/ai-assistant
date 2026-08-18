package com.aiassistant.features.home.data.remote

import com.aiassistant.common.core.SystemHealth
import com.aiassistant.common.network.HealthDto
import com.aiassistant.common.network.NetworkConfig
import com.aiassistant.features.home.data.mapper.SystemDataMapper.toDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface SystemRemoteDataSource {
    suspend fun checkHealth(): SystemHealth
}

class SystemRemoteDataSourceImpl(
    private val httpClient: HttpClient,
) : SystemRemoteDataSource {

    override suspend fun checkHealth(): SystemHealth {
        val response: HealthDto = httpClient.get(NetworkConfig.HEALTH_PATH).body()
        return response.toDomain()
    }
}
