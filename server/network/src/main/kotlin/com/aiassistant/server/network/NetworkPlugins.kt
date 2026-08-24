package com.aiassistant.server.network

import com.aiassistant.server.core.ApiHeaders
import com.aiassistant.server.core.TracingHeaders
import com.aiassistant.server.core.TracingMdc
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

@Serializable
data class ErrorResponse(
    val error: String,
    val requestId: String? = null,
)

fun Application.configureNetwork() {
    configureRequestTracing()

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            },
        )
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        callIdMdc(TracingMdc.REQUEST_ID)
        mdc(TracingMdc.TRACE_ID) { it.requestContextOrNull?.traceId }
        mdc(TracingMdc.SPAN_ID) { it.requestContextOrNull?.spanId }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(ApiHeaders.IDEMPOTENCY_KEY)
        allowHeader(TracingHeaders.REQUEST_ID)
        allowHeader(TracingHeaders.TRACE_ID)
        allowHeader(TracingHeaders.TRACEPARENT)
        exposeHeader(TracingHeaders.REQUEST_ID)
        exposeHeader(TracingHeaders.TRACE_ID)
        exposeHeader(TracingHeaders.TRACEPARENT)
        anyHost()
    }

    install(StatusPages) {
        exception<ContentTransformationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = cause.message ?: "Invalid request body",
                    requestId = call.callId,
                ),
            )
        }
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = cause.message ?: "Invalid request",
                    requestId = call.callId,
                ),
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = cause.message ?: "Internal server error",
                    requestId = call.callId,
                ),
            )
        }
    }
}
