package com.aiassistant.server.gateway.routes

import com.aiassistant.server.application.conversation.CreateConversationUseCase
import com.aiassistant.server.application.conversation.GetConversationUseCase
import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationInsert
import com.aiassistant.server.application.conversation.domain.ConversationRepository
import com.aiassistant.server.core.ApiHeaders
import com.aiassistant.server.gateway.ConversationResponse
import com.aiassistant.server.gateway.CreateConversationRequest
import com.aiassistant.server.network.ErrorResponse
import com.aiassistant.server.network.configureNetwork
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConversationRoutesTest {

    @Test
    fun `POST conversations returns 201 created`() = testApplication {
        val repository = InMemoryConversationRepository()
        installConversationRoutes(repository)

        val client = jsonClient()
        val response = client.createConversation("Planning")
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
        val existing = repository.insert("Existing")
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
        val response = client.createConversation("   ")
        val body = response.body<ErrorResponse>()

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Invalid title", body.error)
    }

    @Test
    fun `POST conversations returns 400 when Idempotency-Key is missing`() = testApplication {
        installConversationRoutes(InMemoryConversationRepository())

        val client = jsonClient()
        val response = client.post("/api/v1/conversations") {
            contentType(ContentType.Application.Json)
            setBody(CreateConversationRequest("Planning"))
        }
        val body = response.body<ErrorResponse>()

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Missing Idempotency-Key", body.error)
    }

    @Test
    fun `POST conversations returns 400 when Idempotency-Key is malformed`() = testApplication {
        installConversationRoutes(InMemoryConversationRepository())

        val client = jsonClient()
        val response = client.post("/api/v1/conversations") {
            contentType(ContentType.Application.Json)
            header(ApiHeaders.IDEMPOTENCY_KEY, "not-a-uuid")
            setBody(CreateConversationRequest("Planning"))
        }
        val body = response.body<ErrorResponse>()

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Malformed Idempotency-Key", body.error)
    }

    @Test
    fun `POST conversations sequential duplicates return the original conversation`() = testApplication {
        val repository = InMemoryConversationRepository()
        installConversationRoutes(repository)

        val client = jsonClient()
        val key = UUID.randomUUID()
        val first = client.createConversation("Planning", key)
        val firstBody = first.body<ConversationResponse>()
        val second = client.createConversation("Planning", key)
        val secondBody = second.body<ConversationResponse>()

        assertEquals(HttpStatusCode.Created, first.status)
        assertEquals(HttpStatusCode.OK, second.status)
        assertEquals(firstBody, secondBody)
        assertEquals(1, repository.size)
    }

    @Test
    fun `POST conversations concurrent duplicate keys create one conversation`() = testApplication {
        val repository = InMemoryConversationRepository(createDelayMillis = 50)
        installConversationRoutes(repository)

        val client = jsonClient()
        val key = UUID.randomUUID()

        val (first, second) = coroutineScope {
            val left = async { client.createConversation("Planning", key) }
            val right = async { client.createConversation("Planning", key) }
            left.await() to right.await()
        }
        val bodies = listOf(first, second).map { it.body<ConversationResponse>() }
        val statuses = setOf(first.status, second.status)

        assertEquals(setOf(HttpStatusCode.Created, HttpStatusCode.OK), statuses)
        assertEquals(1, bodies.map { it.id }.toSet().size)
        assertEquals(bodies[0], bodies[1])
        assertEquals(1, repository.size)
        assertEquals(1, repository.insertCount)
    }

    @Test
    fun `POST conversations timeout retry with the same key returns the created conversation`() = testApplication {
        val repository = InMemoryConversationRepository()
        installConversationRoutes(repository)

        val client = jsonClient()
        val key = UUID.randomUUID()

        val first = client.createConversation("Planning", key)
        val created = first.body<ConversationResponse>()
        assertEquals(HttpStatusCode.Created, first.status)

        // Client timed out after the server committed, then retries with the same key.
        val retry = client.createConversation("Planning", key)
        val existing = retry.body<ConversationResponse>()

        assertEquals(HttpStatusCode.OK, retry.status)
        assertEquals(created, existing)
        assertEquals(1, repository.size)
        assertEquals(1, repository.insertCount)
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

private suspend fun HttpClient.createConversation(
    title: String,
    idempotencyKey: UUID = UUID.randomUUID(),
): HttpResponse = post("/api/v1/conversations") {
    contentType(ContentType.Application.Json)
    header(ApiHeaders.IDEMPOTENCY_KEY, idempotencyKey.toString())
    setBody(CreateConversationRequest(title))
}

private class InMemoryConversationRepository(
    private val createDelayMillis: Long = 0,
) : ConversationRepository {
    private val lock = Mutex()
    private val byId = linkedMapOf<UUID, Conversation>()
    private val byIdempotencyKey = linkedMapOf<UUID, UUID>()
    private val inserts = AtomicInteger(0)

    val size: Int get() = byId.size
    val insertCount: Int get() = inserts.get()

    suspend fun insert(title: String): Conversation {
        return create(title, UUID.randomUUID()).conversation
    }

    override suspend fun create(title: String, idempotencyKey: UUID): ConversationInsert {
        if (createDelayMillis > 0) {
            delay(createDelayMillis)
        }
        return lock.withLock {
            val existingId = byIdempotencyKey[idempotencyKey]
            if (existingId != null) {
                return@withLock ConversationInsert.AlreadyExists(byId.getValue(existingId))
            }
            val conversation = Conversation(
                id = UUID.randomUUID(),
                title = title,
                createdAt = Instant.parse("2026-08-21T11:05:00Z"),
            )
            byId[conversation.id] = conversation
            byIdempotencyKey[idempotencyKey] = conversation.id
            inserts.incrementAndGet()
            ConversationInsert.Inserted(conversation)
        }
    }

    override suspend fun findById(id: UUID): Conversation? = lock.withLock { byId[id] }
}

private class FailingConversationRepository : ConversationRepository {
    override suspend fun create(title: String, idempotencyKey: UUID): ConversationInsert {
        error("database unavailable")
    }

    override suspend fun findById(id: UUID): Conversation? {
        error("database unavailable")
    }
}
