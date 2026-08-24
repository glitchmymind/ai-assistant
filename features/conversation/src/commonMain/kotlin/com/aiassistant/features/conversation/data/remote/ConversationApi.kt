package com.aiassistant.features.conversation.data.remote

import com.aiassistant.common.network.NetworkConfig
import com.aiassistant.features.conversation.data.model.CreateConversationRequestDto
import com.aiassistant.features.conversation.data.model.UpdateConversationRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

interface ConversationApi {
    suspend fun create(title: String, idempotencyKey: String): HttpResponse
    suspend fun list(): HttpResponse
    suspend fun get(id: String): HttpResponse
    suspend fun update(id: String, title: String): HttpResponse
}

class ConversationApiImpl(
    private val httpClient: HttpClient,
) : ConversationApi {

    override suspend fun create(title: String, idempotencyKey: String): HttpResponse {
        return httpClient.post(NetworkConfig.CONVERSATIONS_PATH) {
            header(NetworkConfig.IDEMPOTENCY_KEY_HEADER, idempotencyKey)
            setBody(CreateConversationRequestDto(title))
        }
    }

    override suspend fun list(): HttpResponse {
        return httpClient.get(NetworkConfig.CONVERSATIONS_PATH)
    }

    override suspend fun get(id: String): HttpResponse {
        return httpClient.get(NetworkConfig.conversationPath(id))
    }

    override suspend fun update(id: String, title: String): HttpResponse {
        return httpClient.patch(NetworkConfig.conversationPath(id)) {
            setBody(UpdateConversationRequestDto(title))
        }
    }
}
