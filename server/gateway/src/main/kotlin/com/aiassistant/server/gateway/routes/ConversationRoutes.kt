package com.aiassistant.server.gateway.routes

import com.aiassistant.server.application.conversation.CreateConversationResult
import com.aiassistant.server.application.conversation.CreateConversationUseCase
import com.aiassistant.server.application.conversation.GetConversationResult
import com.aiassistant.server.application.conversation.GetConversationUseCase
import com.aiassistant.server.gateway.CreateConversationRequest
import com.aiassistant.server.gateway.toResponse
import com.aiassistant.server.network.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.ktor.ext.getKoin
import java.util.UUID

fun Route.conversationRoutes(
    createConversation: CreateConversationUseCase? = null,
    getConversation: GetConversationUseCase? = null,
) {
    post("/conversations") {
        val useCase = createConversation ?: call.application.getKoin().get()
        val request = call.receive<CreateConversationRequest>()
        when (val result = useCase(request.title)) {
            is CreateConversationResult.Created -> call.respond(
                HttpStatusCode.Created,
                result.conversation.toResponse(),
            )
            CreateConversationResult.InvalidTitle -> call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Invalid title",
                    requestId = call.callId,
                ),
            )
        }
    }

    get("/conversations/{id}") {
        val useCase = getConversation ?: call.application.getKoin().get()
        val rawId = call.parameters["id"].orEmpty()
        val id = rawId.toUuidOrNull()
        if (id == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Malformed UUID",
                    requestId = call.callId,
                ),
            )
            return@get
        }

        when (val result = useCase(id)) {
            is GetConversationResult.Found -> call.respond(
                HttpStatusCode.OK,
                result.conversation.toResponse(),
            )
            GetConversationResult.NotFound -> call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "Conversation not found",
                    requestId = call.callId,
                ),
            )
        }
    }
}

private fun String.toUuidOrNull(): UUID? {
    return try {
        val uuid = UUID.fromString(this)
        uuid.takeIf { it.toString() == lowercase() }
    } catch (_: IllegalArgumentException) {
        null
    }
}
