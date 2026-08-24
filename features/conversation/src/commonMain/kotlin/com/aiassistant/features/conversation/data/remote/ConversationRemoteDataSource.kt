package com.aiassistant.features.conversation.data.remote

import io.ktor.client.statement.HttpResponse

interface ConversationRemoteDataSource {
    suspend fun create(title: String, idempotencyKey: String): HttpResponse
    suspend fun list(): HttpResponse
    suspend fun get(id: String): HttpResponse
    suspend fun update(id: String, title: String): HttpResponse
}

class ConversationRemoteDataSourceImpl(
    private val conversationApi: ConversationApi,
) : ConversationRemoteDataSource {

    override suspend fun create(title: String, idempotencyKey: String): HttpResponse =
        conversationApi.create(title, idempotencyKey)

    override suspend fun list(): HttpResponse = conversationApi.list()

    override suspend fun get(id: String): HttpResponse = conversationApi.get(id)

    override suspend fun update(id: String, title: String): HttpResponse =
        conversationApi.update(id, title)
}
