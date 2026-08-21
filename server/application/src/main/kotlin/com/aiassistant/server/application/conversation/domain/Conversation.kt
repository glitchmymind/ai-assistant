package com.aiassistant.server.application.conversation.domain

import java.time.Instant
import java.util.UUID

data class Conversation(
    val id: UUID,
    val title: String,
    val createdAt: Instant,
)
