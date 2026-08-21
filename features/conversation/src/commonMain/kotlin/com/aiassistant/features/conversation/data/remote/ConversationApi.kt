package com.aiassistant.features.conversation.data.remote

import com.aiassistant.common.network.NetworkConfig
import com.aiassistant.features.conversation.data.model.CreateConversationRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

interface ConversationApi {
    suspend fun create(title: String): HttpResponse
    suspend fun get(id: String): HttpResponse
}

class ConversationApiImpl(
    private val httpClient: HttpClient,
) : ConversationApi {

    override suspend fun create(title: String): HttpResponse {
        return httpClient.post(NetworkConfig.CONVERSATIONS_PATH) {
            setBody(CreateConversationRequestDto(title))
        }
    }

    override suspend fun get(id: String): HttpResponse {
        return httpClient.get(NetworkConfig.conversationPath(id))
    }
}
