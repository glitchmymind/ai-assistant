package com.aiassistant.common.di

import com.aiassistant.common.network.createHttpClient
import com.aiassistant.features.home.di.homeModule
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val networkModule: Module = module {
    single<HttpClient> { createHttpClient() }
}

val appModules: List<Module> = listOf(
    networkModule,
    homeModule,
)

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModules)
    }
}
