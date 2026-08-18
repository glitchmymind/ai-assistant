package com.aiassistant.common.di

import com.aiassistant.common.core.AppEnvironment
import com.aiassistant.common.core.apiBaseUrl
import com.aiassistant.common.network.createHttpClient
import com.aiassistant.common.network.localLoopbackHost
import com.aiassistant.features.home.di.homeModule
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val networkModule: Module = module {
    single<HttpClient> {
        val environment = get<AppEnvironment>()
        createHttpClient(environment.apiBaseUrl(localLoopbackHost()))
    }
}

val appModules: List<Module> = listOf(
    networkModule,
    homeModule,
)

fun initKoin(
    environment: AppEnvironment = AppEnvironment.current,
    appDeclaration: KoinAppDeclaration = {},
) {
    startKoin {
        appDeclaration()
        modules(
            module { single { environment } },
            networkModule,
            homeModule,
        )
    }
}
