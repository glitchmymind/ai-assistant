package com.aiassistant.server.core

import java.security.SecureRandom

data class RequestContext(
    val requestId: String,
    val traceId: String,
    val spanId: String,
    val parentSpanId: String? = null,
) {
    fun toTraceparent(): String = "00-$traceId-$spanId-01"

    companion object {
        fun create(
            requestId: String,
            traceparent: String?,
            traceIdHeader: String? = null,
        ): RequestContext {
            val parsed = parseTraceparent(traceparent)
            if (parsed != null) {
                return RequestContext(
                    requestId = requestId,
                    traceId = parsed.first,
                    spanId = generateSpanId(),
                    parentSpanId = parsed.second,
                )
            }
            val headerTraceId = parseTraceId(traceIdHeader)
            return RequestContext(
                requestId = requestId,
                traceId = headerTraceId ?: generateTraceId(),
                spanId = generateSpanId(),
            )
        }
    }
}

private val secureRandom = SecureRandom()
private val hex32 = Regex("^[0-9a-f]{32}$")
private val hex16 = Regex("^[0-9a-f]{16}$")

private fun parseTraceparent(header: String?): Pair<String, String>? {
    if (header.isNullOrBlank()) return null
    val parts = header.trim().lowercase().split("-")
    if (parts.size != 4) return null
    val version = parts[0]
    val traceId = parts[1]
    val parentSpanId = parts[2]
    if (version != "00") return null
    if (!isValidTraceId(traceId) || !isValidSpanId(parentSpanId)) return null
    return traceId to parentSpanId
}

private fun parseTraceId(header: String?): String? {
    if (header.isNullOrBlank()) return null
    val traceId = header.trim().lowercase()
    return traceId.takeIf(::isValidTraceId)
}

private fun isValidTraceId(value: String): Boolean =
    hex32.matches(value) && value.any { it != '0' }

private fun isValidSpanId(value: String): Boolean =
    hex16.matches(value) && value.any { it != '0' }

private fun generateTraceId(): String = randomHex(16)

private fun generateSpanId(): String = randomHex(8)

private fun randomHex(byteCount: Int): String {
    val bytes = ByteArray(byteCount)
    secureRandom.nextBytes(bytes)
    return bytes.toHexString()
}
