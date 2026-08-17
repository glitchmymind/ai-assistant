package com.aiassistant.common.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createHttpClient(baseUrl: String): HttpClient = HttpClient(Darwin) {
    installKtorDefaults(baseUrl)
}
