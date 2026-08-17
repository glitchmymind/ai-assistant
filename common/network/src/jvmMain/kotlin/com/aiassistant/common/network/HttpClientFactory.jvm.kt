package com.aiassistant.common.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createHttpClient(baseUrl: String): HttpClient = HttpClient(OkHttp) {
    installKtorDefaults(baseUrl)
}
