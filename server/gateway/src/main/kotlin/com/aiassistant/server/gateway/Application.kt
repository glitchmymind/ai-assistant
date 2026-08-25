package com.aiassistant.server.gateway

import com.aiassistant.server.application.outbox.KafkaMessageSender
import com.aiassistant.server.application.outbox.OutboxPublisherWorker
import com.aiassistant.server.core.AppConfig
import com.aiassistant.server.db.DatabaseFactory
import com.aiassistant.server.di.configureDi
import com.aiassistant.server.network.configureNetwork
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import org.koin.ktor.ext.getKoin

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
    configureOutboxPublisher()
}

fun Application.configureOutboxPublisher() {
    val koin = getKoin()
    val worker = koin.get<OutboxPublisherWorker>()
    val sender = koin.get<KafkaMessageSender>()
    launch(CoroutineName("outbox-publisher")) {
        worker.run()
    }
    monitor.subscribe(ApplicationStopping) {
        (sender as? AutoCloseable)?.close()
    }
}
