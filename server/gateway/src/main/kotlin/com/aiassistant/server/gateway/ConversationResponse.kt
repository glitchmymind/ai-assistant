package com.aiassistant.server.gateway

import com.aiassistant.server.application.conversation.domain.Conversation
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter

@Serializable
data class ConversationResponse(
    val id: String,
    val title: String,
    val createdAt: String,
)

@Serializable
data class ConversationListResponse(
    val conversations: List<ConversationResponse>,
)

fun Conversation.toResponse(): ConversationResponse = ConversationResponse(
    id = id.toString(),
    title = title,
    createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt),
)

fun List<Conversation>.toListResponse(): ConversationListResponse = ConversationListResponse(
    conversations = map { it.toResponse() },
)
