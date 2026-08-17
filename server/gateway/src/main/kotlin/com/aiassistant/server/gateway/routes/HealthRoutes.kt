package com.aiassistant.server.gateway.routes

import com.aiassistant.server.gateway.HealthResponse
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.healthRoutes() {
    get("/health") {
        call.respond(
            HealthResponse(
                status = "ok",
                service = "ai-assistant-api",
                version = "0.1.0",
            ),
        )
    }
}
