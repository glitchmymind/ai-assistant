package com.aiassistant.features.home.domain

sealed interface SystemHealthResult {

    data class Healthy(
        val version: String,
    ) : SystemHealthResult

    data object Unavailable : SystemHealthResult
}
