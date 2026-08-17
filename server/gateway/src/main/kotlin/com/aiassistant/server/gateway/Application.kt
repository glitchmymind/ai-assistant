package com.aiassistant.server.gateway

import com.aiassistant.server.core.AppConfig
import com.aiassistant.server.db.DatabaseFactory
import com.aiassistant.server.di.configureDi
import com.aiassistant.server.network.configureNetwork
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(
        factory = Netty,
        port = AppConfig.port,
        host = AppConfig.host,
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    configureDi()
    configureNetwork()
    DatabaseFactory.init()
    configureRouting()
}
