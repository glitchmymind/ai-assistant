package com.aiassistant.server.gateway

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(
    val title: String,
)
