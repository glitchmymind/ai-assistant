package com.aiassistant.server.db

import com.aiassistant.server.core.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
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

    fun migrate() {
        Flyway.configure()
            .dataSource(AppConfig.jdbcUrl, AppConfig.dbUser, AppConfig.dbPassword)
            .locations("classpath:db/migration")
            .load()
            .migrate()
        logger.info("PostgreSQL migrations are up to date")
    }

    fun init() {
        try {
            connect()
            migrate()
        } catch (error: Exception) {
            logger.warn("Database is unavailable: ${error.message}")
        }
    }
}
