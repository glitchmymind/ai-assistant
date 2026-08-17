package com.aiassistant.server.agent

interface LanguageModel {
    suspend fun generate(request: LlmRequest): LlmResponse
}
