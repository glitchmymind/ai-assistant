package com.aiassistant.features.conversation.data.remote

import io.ktor.client.statement.HttpResponse

interface ConversationRemoteDataSource {
    suspend fun create(title: String): HttpResponse
    suspend fun get(id: String): HttpResponse
}

class ConversationRemoteDataSourceImpl(
    private val conversationApi: ConversationApi,
) : ConversationRemoteDataSource {

    override suspend fun create(title: String): HttpResponse = conversationApi.create(title)

    override suspend fun get(id: String): HttpResponse = conversationApi.get(id)
}
