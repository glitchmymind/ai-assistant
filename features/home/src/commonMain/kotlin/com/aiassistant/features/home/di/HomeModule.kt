package com.aiassistant.features.home.di

import com.aiassistant.features.home.data.SystemRepositoryImpl
import com.aiassistant.features.home.data.local.SystemLocalDataSource
import com.aiassistant.features.home.data.local.SystemLocalDataSourceImpl
import com.aiassistant.features.home.data.remote.SystemRemoteDataSource
import com.aiassistant.features.home.data.remote.SystemRemoteDataSourceImpl
import com.aiassistant.features.home.domain.CheckHealthUseCase
import com.aiassistant.features.home.domain.SystemRepository
import com.aiassistant.features.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    single<SystemRemoteDataSource> { SystemRemoteDataSourceImpl(get()) }
    single<SystemLocalDataSource> { SystemLocalDataSourceImpl() }
    single<SystemRepository> { SystemRepositoryImpl(get(), get()) }
    factory { CheckHealthUseCase(get()) }
    viewModelOf(::HomeViewModel)
}
