package com.aiassistant.server.core

object AppConfig {
    const val SERVICE_NAME = "ai-assistant-api"
    const val VERSION = "0.1.0"

    private val fileValues: Map<String, String> = EnvLoader.load()

    val host: String = env("HOST", "0.0.0.0")
    val port: Int = env("PORT", "8080").toInt()

    val dbHost: String by lazy { env("POSTGRES_HOST", "localhost") }
    val dbPort: String by lazy { env("POSTGRES_PORT", "5432") }
    val dbName: String by lazy { required("POSTGRES_DB") }
    val dbUser: String by lazy { required("POSTGRES_USER") }
    val dbPassword: String by lazy { required("POSTGRES_PASSWORD") }

    val jdbcUrl: String by lazy {
        env(
            key = "DATABASE_URL",
            default = "jdbc:postgresql://$dbHost:$dbPort/$dbName",
        )
    }

    val redisHost: String by lazy { env("REDIS_HOST", "localhost") }
    val redisPort: Int by lazy { env("REDIS_PORT", "6379").toInt() }

    private fun env(key: String, default: String): String {
        return System.getenv(key) ?: fileValues[key] ?: default
    }

    private fun required(key: String): String {
        return System.getenv(key) ?: fileValues[key]
            ?: error("Missing $key. Copy .env.example to .env and set credentials.")
    }
}
