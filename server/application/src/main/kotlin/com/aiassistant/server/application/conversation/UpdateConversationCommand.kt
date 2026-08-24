package com.aiassistant.server.application.conversation

import java.util.UUID

data class UpdateConversationCommand(
    val id: UUID,
    val title: String,
)
