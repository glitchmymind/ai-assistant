package com.aiassistant.server.di

import com.aiassistant.server.core.AppConfig
import com.aiassistant.server.db.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val coreModule = module {
    single { AppConfig }
}

val dbModule = module {
    single { DatabaseFactory }
}

val serverModules = listOf(coreModule, dbModule)

fun Application.configureDi() {
    install(Koin) {
        slf4jLogger()
        modules(serverModules)
    }
}
