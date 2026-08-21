package com.aiassistant.server.gateway.routes

import com.aiassistant.server.application.conversation.CreateConversationUseCase
import com.aiassistant.server.application.conversation.GetConversationUseCase
import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationRepository
import com.aiassistant.server.gateway.ConversationResponse
import com.aiassistant.server.gateway.CreateConversationRequest
import com.aiassistant.server.network.ErrorResponse
import com.aiassistant.server.network.configureNetwork
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConversationRoutesTest {

    @Test
    fun `POST conversations returns 201 created`() = testApplication {
        val repository = InMemoryConversationRepository()
        installConversationRoutes(repository)

        val client = jsonClient()
        val response = client.post("/api/v1/conversations") {
            contentType(ContentType.Application.Json)
            setBody(CreateConversationRequest("Planning"))
        }
        val body = response.body<ConversationResponse>()

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Planning", body.title)
        assertTrue(body.id.isNotBlank())
        assertTrue(body.createdAt.endsWith("Z"))
        Instant.parse(body.createdAt)
    }

    @Test
    fun `GET conversation returns 200`() = testApplication {
        val repository = InMemoryConversationRepository()
        val existing = repository.create("Existing")
        installConversationRoutes(repository)

        val client = jsonClient()
        val response = client.get("/api/v1/conversations/${existing.id}")
        val body = response.body<ConversationResponse>()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(existing.id.toString(), body.id)
        assertEquals("Existing", body.title)
        assertEquals("2026-08-21T11:05:00Z", body.createdAt)
    }

    @Test
    fun `POST conversations returns 400 for blank title`() = testApplication {
        installConversationRoutes(InMemoryConversationRepository())

        val client = jsonClient()
        val response = client.post("/api/v1/conversations") {
            contentType(ContentType.Application.Json)
            setBody(CreateConversationRequest("   "))
        }
        val body = response.body<ErrorResponse>()

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Invalid title", body.error)
    }

    @Test
    fun `GET conversation returns 400 for malformed UUID`() = testApplication {
        installConversationRoutes(InMemoryConversationRepository())

        val client = jsonClient()
        val response = client.get("/api/v1/conversations/not-a-uuid")
        val body = response.body<ErrorResponse>()

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Malformed UUID", body.error)
    }

    @Test
    fun `GET conversation returns 404 when missing`() = testApplication {
        installConversationRoutes(InMemoryConversationRepository())

        val client = jsonClient()
        val missingId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val response = client.get("/api/v1/conversations/$missingId")
        val body = response.body<ErrorResponse>()

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("Conversation not found", body.error)
    }

    @Test
    fun `GET conversation returns 500 when repository fails`() = testApplication {
        installConversationRoutes(FailingConversationRepository())

        val client = jsonClient()
        val id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val response = client.get("/api/v1/conversations/$id")

        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.installConversationRoutes(
        repository: ConversationRepository,
    ) {
        application {
            configureNetwork()
            routing {
                route("/api/v1") {
                    conversationRoutes(
                        createConversation = CreateConversationUseCase(repository),
                        getConversation = GetConversationUseCase(repository),
                    )
                }
            }
        }
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) {
            json()
        }
    }
}

private class InMemoryConversationRepository : ConversationRepository {
    private val store = linkedMapOf<UUID, Conversation>()

    override suspend fun create(title: String): Conversation {
        val conversation = Conversation(
            id = UUID.randomUUID(),
            title = title,
            createdAt = Instant.parse("2026-08-21T11:05:00Z"),
        )
        store[conversation.id] = conversation
        return conversation
    }

    override suspend fun findById(id: UUID): Conversation? = store[id]
}

private class FailingConversationRepository : ConversationRepository {
    override suspend fun create(title: String): Conversation {
        error("database unavailable")
    }

    override suspend fun findById(id: UUID): Conversation? {
        error("database unavailable")
    }
}
