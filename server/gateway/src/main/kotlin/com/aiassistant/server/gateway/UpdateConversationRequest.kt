package com.aiassistant.server.gateway

import kotlinx.serialization.Serializable

@Serializable
data class UpdateConversationRequest(
    val title: String,
)
