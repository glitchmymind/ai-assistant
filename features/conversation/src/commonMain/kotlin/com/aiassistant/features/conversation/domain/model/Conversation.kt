package com.aiassistant.features.conversation.domain.model

import kotlin.time.Instant

data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Instant,
)
