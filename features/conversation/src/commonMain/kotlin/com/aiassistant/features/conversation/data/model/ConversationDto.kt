package com.aiassistant.features.conversation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    val title: String,
    val createdAt: String,
)

@Serializable
data class ConversationListDto(
    val conversations: List<ConversationDto>,
)

@Serializable
data class CreateConversationRequestDto(
    val title: String,
)

@Serializable
data class UpdateConversationRequestDto(
    val title: String,
)

@Serializable
data class ApiErrorDto(
    val error: String,
    val requestId: String? = null,
)
