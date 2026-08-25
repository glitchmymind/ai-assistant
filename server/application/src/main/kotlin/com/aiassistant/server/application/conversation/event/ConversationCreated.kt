package com.aiassistant.server.application.conversation.event

import com.aiassistant.server.application.conversation.domain.Conversation
import com.aiassistant.server.application.outbox.NewDomainEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter

object ConversationCreated {
    const val AGGREGATE_TYPE = "Conversation"
    const val EVENT_TYPE = "ConversationCreated"
    const val EVENT_VERSION = 1
}

fun Conversation.toCreatedEvent(
    json: Json = ConversationEventJson,
): NewDomainEvent = NewDomainEvent(
    aggregateType = ConversationCreated.AGGREGATE_TYPE,
    aggregateId = id,
    eventType = ConversationCreated.EVENT_TYPE,
    eventVersion = ConversationCreated.EVENT_VERSION,
    payload = json.encodeToString(
        ConversationCreatedPayload.serializer(),
        ConversationCreatedPayload(
            id = id.toString(),
            title = title,
            createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt),
        ),
    ),
)

@Serializable
internal data class ConversationCreatedPayload(
    val id: String,
    val title: String,
    val createdAt: String,
)

private val ConversationEventJson = Json {
    encodeDefaults = true
}
