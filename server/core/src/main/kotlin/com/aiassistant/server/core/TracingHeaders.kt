package com.aiassistant.server.core

object TracingHeaders {
    const val REQUEST_ID = "X-Request-ID"
    const val TRACE_ID = "X-Trace-Id"
    const val TRACEPARENT = "traceparent"
}

object ApiHeaders {
    const val IDEMPOTENCY_KEY = "Idempotency-Key"
}

object TracingMdc {
    const val REQUEST_ID = "requestId"
    const val TRACE_ID = "traceId"
    const val SPAN_ID = "spanId"
}
