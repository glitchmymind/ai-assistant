package com.aiassistant.features.home.data.remote

import com.aiassistant.common.network.HealthDto
import com.aiassistant.common.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface SystemApi {
    suspend fun checkHealth(): HealthDto
}

class SystemApiImpl(
    private val httpClient: HttpClient,
) : SystemApi {

    override suspend fun checkHealth(): HealthDto {
        return httpClient.get(NetworkConfig.HEALTH_PATH).body()
    }
}
