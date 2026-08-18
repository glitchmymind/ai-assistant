package com.aiassistant.server.network

import com.aiassistant.server.core.RequestContext
import com.aiassistant.server.core.TracingHeaders
import com.aiassistant.server.core.TracingMdc
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.header
import io.ktor.server.response.header
import io.ktor.util.AttributeKey
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import java.util.UUID

val RequestContextKey = AttributeKey<RequestContext>("RequestContext")

val ApplicationCall.requestContext: RequestContext
    get() = attributes[RequestContextKey]

val ApplicationCall.requestContextOrNull: RequestContext?
    get() = attributes.getOrNull(RequestContextKey)

fun Application.configureRequestTracing() {
    install(CallId) {
        retrieveFromHeader(TracingHeaders.REQUEST_ID)
        generate { UUID.randomUUID().toString() }
        verify { it.isNotBlank() && it.length <= 128 }
        replyToHeader(TracingHeaders.REQUEST_ID)
    }

    intercept(ApplicationCallPipeline.Setup) {
        val requestId = call.callId ?: UUID.randomUUID().toString()
        val context = RequestContext.create(
            requestId = requestId,
            traceparent = call.request.header(TracingHeaders.TRACEPARENT),
            traceIdHeader = call.request.header(TracingHeaders.TRACE_ID),
        )
        call.attributes.put(RequestContextKey, context)
        call.response.header(TracingHeaders.TRACE_ID, context.traceId)
        call.response.header(TracingHeaders.TRACEPARENT, context.toTraceparent())

        MDC.put(TracingMdc.REQUEST_ID, context.requestId)
        MDC.put(TracingMdc.TRACE_ID, context.traceId)
        MDC.put(TracingMdc.SPAN_ID, context.spanId)
        try {
            withContext(MDCContext()) {
                proceed()
            }
        } finally {
            MDC.remove(TracingMdc.REQUEST_ID)
            MDC.remove(TracingMdc.TRACE_ID)
            MDC.remove(TracingMdc.SPAN_ID)
        }
    }
}
