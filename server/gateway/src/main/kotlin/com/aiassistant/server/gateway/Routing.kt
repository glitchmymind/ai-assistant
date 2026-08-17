package com.aiassistant.server.gateway

import com.aiassistant.server.gateway.routes.healthRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        healthRoutes()
    }
}
