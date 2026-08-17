package com.aiassistant.common.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun createHttpClient(baseUrl: String): HttpClient = HttpClient(Js) {
    installKtorDefaults(baseUrl)
}
