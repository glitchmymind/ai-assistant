package com.aiassistant.features.conversation.data

import com.aiassistant.features.conversation.data.mapper.ConversationMapper.toDomain
import com.aiassistant.features.conversation.data.model.ConversationDto
import com.aiassistant.features.conversation.data.remote.ConversationRemoteDataSource
import com.aiassistant.features.conversation.domain.ConversationRepository
import com.aiassistant.features.conversation.domain.model.Conversation
import com.aiassistant.features.conversation.domain.model.ConversationError
import com.aiassistant.features.conversation.domain.model.ConversationResult
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

class ConversationRepositoryImpl(
    private val remoteDataSource: ConversationRemoteDataSource,
) : ConversationRepository {

    override suspend fun create(title: String): ConversationResult<Conversation> {
        return request(
            execute = { remoteDataSource.create(title) },
            mapStatus = { status ->
                when (status) {
                    HttpStatusCode.Created -> null
                    HttpStatusCode.BadRequest -> ConversationError.InvalidTitle
                    else -> ConversationError.Server
                }
            },
        )
    }

    override suspend fun get(id: String): ConversationResult<Conversation> {
        if (!UUID_PATTERN.matches(id)) {
            return ConversationResult.Failure(ConversationError.MalformedId)
        }
        return request(
            execute = { remoteDataSource.get(id) },
            mapStatus = { status ->
                when (status) {
                    HttpStatusCode.OK -> null
                    HttpStatusCode.BadRequest -> ConversationError.MalformedId
                    HttpStatusCode.NotFound -> ConversationError.NotFound
                    else -> ConversationError.Server
                }
            },
        )
    }

    private suspend fun request(
        execute: suspend () -> HttpResponse,
        mapStatus: (HttpStatusCode) -> ConversationError?,
    ): ConversationResult<Conversation> {
        return try {
            val response = execute()
            val error = mapStatus(response.status)
            if (error != null) {
                ConversationResult.Failure(error)
            } else {
                ConversationResult.Success(response.body<ConversationDto>().toDomain())
            }
        } catch (error: ClientRequestException) {
            ConversationResult.Failure(
                mapStatus(error.response.status) ?: ConversationError.Server,
            )
        } catch (_: ServerResponseException) {
            ConversationResult.Failure(ConversationError.Server)
        } catch (_: Exception) {
            ConversationResult.Failure(ConversationError.Network)
        }
    }

    private companion object {
        val UUID_PATTERN = Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
        )
    }
}
