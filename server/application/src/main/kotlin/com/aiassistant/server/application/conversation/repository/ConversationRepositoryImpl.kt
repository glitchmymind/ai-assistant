package com.aiassistant.server.application.conversation.repository

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationInsert
import com.aiassistant.server.application.conversation.domain.ConversationRepository
import com.aiassistant.server.db.ConversationRow
import com.aiassistant.server.db.Conversations
import com.aiassistant.server.db.toConversationRow
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.sql.Connection
import java.util.UUID

class ConversationRepositoryImpl : ConversationRepository {

    override suspend fun create(title: String, idempotencyKey: UUID): ConversationInsert = dbQuery(
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED,
    ) {
        val inserted = Conversations.insertReturning(ignoreErrors = true) {
            it[Conversations.title] = title
            it[Conversations.idempotencyKey] = idempotencyKey
        }.singleOrNull()

        if (inserted != null) {
            return@dbQuery ConversationInsert.Inserted(inserted.toConversationRow().toDomain())
        }

        val existing = Conversations
            .selectAll()
            .where { Conversations.idempotencyKey eq idempotencyKey }
            .single()
            .toConversationRow()
            .toDomain()
        ConversationInsert.AlreadyExists(existing)
    }

    override suspend fun findById(id: UUID): Conversation? = dbQuery {
        Conversations
            .selectAll()
            .where { Conversations.id eq id }
            .singleOrNull()
            ?.toConversationRow()
            ?.toDomain()
    }

    override suspend fun findAll(): List<Conversation> = dbQuery {
        Conversations
            .selectAll()
            .orderBy(Conversations.createdAt to SortOrder.DESC)
            .map { it.toConversationRow().toDomain() }
    }

    override suspend fun update(id: UUID, title: String): Conversation? = dbQuery {
        val updated = Conversations.update({ Conversations.id eq id }) {
            it[Conversations.title] = title
        }
        if (updated == 0) {
            return@dbQuery null
        }
        Conversations
            .selectAll()
            .where { Conversations.id eq id }
            .single()
            .toConversationRow()
            .toDomain()
    }

    private suspend fun <T> dbQuery(
        transactionIsolation: Int? = null,
        block: suspend () -> T,
    ): T = newSuspendedTransaction(
        context = Dispatchers.IO,
        transactionIsolation = transactionIsolation,
    ) { block() }
}

private fun ConversationRow.toDomain() = Conversation(
    id = id,
    title = title,
    createdAt = createdAt.toInstant(),
)
