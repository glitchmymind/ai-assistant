package com.aiassistant.server.core

object AppConfig {
    const val SERVICE_NAME = "ai-assistant-api"
    const val VERSION = "0.1.0"

    val host: String = System.getenv("HOST") ?: "0.0.0.0"
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080

    val jdbcUrl: String = System.getenv("DATABASE_URL")
        ?: "jdbc:postgresql://localhost:5432/ai_assistant"
    val dbUser: String = System.getenv("DATABASE_USER") ?: "postgres"
    val dbPassword: String = System.getenv("DATABASE_PASSWORD") ?: "postgres"
}
