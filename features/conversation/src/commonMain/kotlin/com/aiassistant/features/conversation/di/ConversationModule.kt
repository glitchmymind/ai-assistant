package com.aiassistant.features.conversation.di

import com.aiassistant.features.conversation.data.ConversationRepositoryImpl
import com.aiassistant.features.conversation.data.remote.ConversationApi
import com.aiassistant.features.conversation.data.remote.ConversationApiImpl
import com.aiassistant.features.conversation.data.remote.ConversationRemoteDataSource
import com.aiassistant.features.conversation.data.remote.ConversationRemoteDataSourceImpl
import com.aiassistant.features.conversation.domain.ConversationRepository
import com.aiassistant.features.conversation.presentation.viewmodel.ConversationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val conversationModule = module {
    single<ConversationApi> { ConversationApiImpl(get()) }
    single<ConversationRemoteDataSource> { ConversationRemoteDataSourceImpl(get()) }
    single<ConversationRepository> { ConversationRepositoryImpl(get()) }
    viewModelOf(::ConversationViewModel)
}
