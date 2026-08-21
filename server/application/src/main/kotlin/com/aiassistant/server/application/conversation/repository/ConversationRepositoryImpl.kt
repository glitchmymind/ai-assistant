package com.aiassistant.server.application.conversation.repository

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.conversation.domain.ConversationRepository
import com.aiassistant.server.db.ConversationRow
import com.aiassistant.server.db.Conversations
import com.aiassistant.server.db.toConversationRow
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ConversationRepositoryImpl : ConversationRepository {

    override suspend fun create(title: String): Conversation = dbQuery {
        Conversations.insertReturning {
            it[Conversations.title] = title
        }.single().toConversationRow().toDomain()
    }

    override suspend fun findById(id: UUID): Conversation? = dbQuery {
        Conversations
            .selectAll()
            .where { Conversations.id eq id }
            .singleOrNull()
            ?.toConversationRow()
            ?.toDomain()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

private fun ConversationRow.toDomain() = Conversation(
    id = id,
    title = title,
    createdAt = createdAt.toInstant(),
)
