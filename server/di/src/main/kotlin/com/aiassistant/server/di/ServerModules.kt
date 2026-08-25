package com.aiassistant.server.di

import com.aiassistant.server.application.conversation.CreateConversationUseCase
import com.aiassistant.server.application.conversation.GetConversationUseCase
import com.aiassistant.server.application.conversation.ListConversationsUseCase
import com.aiassistant.server.application.conversation.UpdateConversationUseCase
import com.aiassistant.server.application.conversation.cache.RedisConversationCache
import com.aiassistant.server.application.conversation.cache.RedisFactory
import com.aiassistant.server.application.conversation.cache.RedisStringStore
import com.aiassistant.server.application.conversation.domain.ConversationCache
import com.aiassistant.server.application.conversation.domain.ConversationRepository
import com.aiassistant.server.application.conversation.repository.ConversationRepositoryImpl
import com.aiassistant.server.application.outbox.EventPublisher
import com.aiassistant.server.application.outbox.KafkaEventPublisher
import com.aiassistant.server.application.outbox.KafkaFactory
import com.aiassistant.server.application.outbox.KafkaMessageSender
import com.aiassistant.server.application.outbox.OutboxPublisher
import com.aiassistant.server.application.outbox.OutboxPublisherWorker
import com.aiassistant.server.application.outbox.OutboxRepository
import com.aiassistant.server.application.outbox.repository.OutboxRepositoryImpl
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

val conversationModule = module {
    single<OutboxRepository> { OutboxRepositoryImpl() }
    single<ConversationRepository> { ConversationRepositoryImpl(get()) }
    single<RedisStringStore> { RedisFactory.createStore() }
    single<ConversationCache> { RedisConversationCache(get()) }
    single<KafkaMessageSender> { KafkaFactory.createSender() }
    single<EventPublisher> {
        KafkaEventPublisher(
            sender = get(),
            topic = AppConfig.kafkaConversationEventsTopic,
        )
    }
    single { OutboxPublisher(get(), get()) }
    single { OutboxPublisherWorker(get()) }
    factory { CreateConversationUseCase(get()) }
    factory { GetConversationUseCase(get(), get()) }
    factory { ListConversationsUseCase(get()) }
    factory { UpdateConversationUseCase(get(), get()) }
}

val serverModules = listOf(coreModule, dbModule, conversationModule)

fun Application.configureDi() {
    install(Koin) {
        slf4jLogger()
        modules(serverModules)
    }
}
