package com.aiassistant.server.db

import com.aiassistant.server.core.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun connect(): Database {
        val hikari = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = AppConfig.jdbcUrl
                username = AppConfig.dbUser
                password = AppConfig.dbPassword
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = 10
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            },
        )
        return Database.connect(hikari)
    }

    fun createSchema() {
        transaction {
            SchemaUtils.create(Users, Conversations, Messages)
            logger.info("PostgreSQL schema is ready")
        }
    }

    fun init() {
        try {
            connect()
            createSchema()
        } catch (error: Exception) {
            logger.warn("Database is unavailable: ${error.message}")
        }
    }
}
