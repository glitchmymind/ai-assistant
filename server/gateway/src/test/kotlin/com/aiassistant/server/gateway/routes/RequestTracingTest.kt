package com.aiassistant.server.gateway.routes

import com.aiassistant.server.core.TracingHeaders
import com.aiassistant.server.gateway.configureRouting
import com.aiassistant.server.network.configureNetwork
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RequestTracingTest {
    @Test
    fun `generates requestId and W3C traceparent when missing`() = testApplication {
        application {
            configureNetwork()
            configureRouting()
        }

        val response = client.get("/api/v1/health")
        val requestId = response.headers[TracingHeaders.REQUEST_ID]
        val traceId = response.headers[TracingHeaders.TRACE_ID]
        val traceparent = response.headers[TracingHeaders.TRACEPARENT]

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(requestId)
        assertTrue(requestId.isNotBlank())
        assertNotNull(traceId)
        assertTrue(traceId.matches(Regex("^[0-9a-f]{32}$")))
        assertNotNull(traceparent)
        assertEquals("00-$traceId-${traceparent.split("-")[2]}-01", traceparent)
    }

    @Test
    fun `echoes incoming request id`() = testApplication {
        application {
            configureNetwork()
            configureRouting()
        }

        val incomingRequestId = "req-support-123"
        val response = client.get("/api/v1/health") {
            header(TracingHeaders.REQUEST_ID, incomingRequestId)
        }

        assertEquals(incomingRequestId, response.headers[TracingHeaders.REQUEST_ID])
    }

    @Test
    fun `keeps incoming W3C trace id and creates a new span`() = testApplication {
        application {
            configureNetwork()
            configureRouting()
        }

        val incomingTraceId = "4bf92f3577b34da6a3ce929d0e0e4736"
        val incomingSpanId = "00f067aa0ba902b7"
        val incoming = "00-$incomingTraceId-$incomingSpanId-01"
        val response = client.get("/api/v1/health") {
            header(TracingHeaders.TRACEPARENT, incoming)
        }

        val traceparent = response.headers[TracingHeaders.TRACEPARENT]
        assertNotNull(traceparent)
        assertEquals(incomingTraceId, response.headers[TracingHeaders.TRACE_ID])
        assertTrue(traceparent.startsWith("00-$incomingTraceId-"))
        assertNotEquals(incoming, traceparent)
        val outgoingSpanId = traceparent.split("-")[2]
        assertNotEquals(incomingSpanId, outgoingSpanId)
    }

    @Test
    fun `uses X-Trace-Id when traceparent is missing`() = testApplication {
        application {
            configureNetwork()
            configureRouting()
        }

        val incomingTraceId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        val response = client.get("/api/v1/health") {
            header(TracingHeaders.TRACE_ID, incomingTraceId)
        }

        assertEquals(incomingTraceId, response.headers[TracingHeaders.TRACE_ID])
        assertTrue(response.headers[TracingHeaders.TRACEPARENT]!!.startsWith("00-$incomingTraceId-"))
    }
}
