package com.aiassistant.common.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun HttpClientConfig<*>.installKtorDefaults(baseUrl: String = defaultBaseUrl()) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
                encodeDefaults = true
            },
        )
    }
    install(Logging) {
        level = LogLevel.INFO
    }
    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
}

expect fun createHttpClient(baseUrl: String = defaultBaseUrl()): HttpClient
