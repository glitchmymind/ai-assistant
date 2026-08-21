package com.aiassistant.features.conversation.data.mapper

import com.aiassistant.features.conversation.data.model.ConversationDto
import com.aiassistant.features.conversation.domain.model.Conversation
import kotlin.time.Instant

object ConversationMapper {
    fun ConversationDto.toDomain(): Conversation = Conversation(
        id = id,
        title = title,
        createdAt = Instant.parse(createdAt),
    )
}
