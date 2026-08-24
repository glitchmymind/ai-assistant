package com.aiassistant.features.conversation.data

import com.aiassistant.features.conversation.data.mapper.ConversationMapper.toDomain
import com.aiassistant.features.conversation.data.model.ApiErrorDto
import com.aiassistant.features.conversation.data.model.ConversationDto
import com.aiassistant.features.conversation.data.model.ConversationListDto
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ConversationRepositoryImpl(
    private val remoteDataSource: ConversationRemoteDataSource,
) : ConversationRepository {

    override suspend fun create(title: String): ConversationResult<Conversation> {
        val idempotencyKey = Uuid.random().toString()
        var lastResult: ConversationResult<Conversation> = ConversationResult.Failure(ConversationError.Network)
        repeat(CREATE_ATTEMPTS) {
            lastResult = request(
                execute = { remoteDataSource.create(title, idempotencyKey) },
                mapError = { response ->
                    when (response.status) {
                        HttpStatusCode.Created, HttpStatusCode.OK -> null
                        HttpStatusCode.BadRequest -> mapCreateBadRequest(response)
                        else -> ConversationError.Server
                    }
                },
                parse = { response -> response.body<ConversationDto>().toDomain() },
            )
            val failure = lastResult as? ConversationResult.Failure
            if (failure == null || failure.error != ConversationError.Network) {
                return lastResult
            }
        }
        return lastResult
    }

    override suspend fun get(id: String): ConversationResult<Conversation> {
        if (!UUID_PATTERN.matches(id)) {
            return ConversationResult.Failure(ConversationError.MalformedId)
        }
        return request(
            execute = { remoteDataSource.get(id) },
            mapError = { response ->
                when (response.status) {
                    HttpStatusCode.OK -> null
                    HttpStatusCode.BadRequest -> ConversationError.MalformedId
                    HttpStatusCode.NotFound -> ConversationError.NotFound
                    else -> ConversationError.Server
                }
            },
            parse = { response -> response.body<ConversationDto>().toDomain() },
        )
    }

    override suspend fun list(): ConversationResult<List<Conversation>> {
        return request(
            execute = { remoteDataSource.list() },
            mapError = { response ->
                when (response.status) {
                    HttpStatusCode.OK -> null
                    else -> ConversationError.Server
                }
            },
            parse = { response ->
                response.body<ConversationListDto>().conversations.map { it.toDomain() }
            },
        )
    }

    override suspend fun update(id: String, title: String): ConversationResult<Conversation> {
        if (!UUID_PATTERN.matches(id)) {
            return ConversationResult.Failure(ConversationError.MalformedId)
        }
        return request(
            execute = { remoteDataSource.update(id, title) },
            mapError = { response ->
                when (response.status) {
                    HttpStatusCode.OK -> null
                    HttpStatusCode.BadRequest -> mapUpdateBadRequest(response)
                    HttpStatusCode.NotFound -> ConversationError.NotFound
                    else -> ConversationError.Server
                }
            },
            parse = { response -> response.body<ConversationDto>().toDomain() },
        )
    }

    private suspend fun <T> request(
        execute: suspend () -> HttpResponse,
        mapError: suspend (HttpResponse) -> ConversationError?,
        parse: suspend (HttpResponse) -> T,
    ): ConversationResult<T> {
        return try {
            val response = execute()
            val error = mapError(response)
            if (error != null) {
                ConversationResult.Failure(error)
            } else {
                ConversationResult.Success(parse(response))
            }
        } catch (error: ClientRequestException) {
            ConversationResult.Failure(
                mapError(error.response) ?: ConversationError.Server,
            )
        } catch (_: ServerResponseException) {
            ConversationResult.Failure(ConversationError.Server)
        } catch (_: Exception) {
            ConversationResult.Failure(ConversationError.Network)
        }
    }

    private suspend fun mapCreateBadRequest(response: HttpResponse): ConversationError {
        val message = runCatching { response.body<ApiErrorDto>().error }.getOrNull()
        return when (message) {
            "Invalid title" -> ConversationError.InvalidTitle
            "Missing Idempotency-Key" -> ConversationError.MissingIdempotencyKey
            "Malformed Idempotency-Key" -> ConversationError.MalformedIdempotencyKey
            else -> ConversationError.Server
        }
    }

    private suspend fun mapUpdateBadRequest(response: HttpResponse): ConversationError {
        val message = runCatching { response.body<ApiErrorDto>().error }.getOrNull()
        return when (message) {
            "Invalid title" -> ConversationError.InvalidTitle
            "Malformed UUID" -> ConversationError.MalformedId
            else -> ConversationError.Server
        }
    }

    private companion object {
        const val CREATE_ATTEMPTS = 3
        val UUID_PATTERN = Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
        )
    }
}
