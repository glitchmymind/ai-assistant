package com.aiassistant.server.application.conversation

import java.util.UUID

data class CreateConversationCommand(
    val title: String,
    val idempotencyKey: UUID,
)
