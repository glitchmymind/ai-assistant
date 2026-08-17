package com.aiassistant.server.agent

data class Message(
    val role: Role,
    val content: String,
)
