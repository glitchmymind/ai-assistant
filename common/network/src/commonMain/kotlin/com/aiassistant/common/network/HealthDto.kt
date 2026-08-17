package com.aiassistant.common.network

import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(
    val status: String,
    val service: String,
    val version: String,
)
