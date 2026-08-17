package com.aiassistant.common.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

object AppRoutes {
    val startDestination: Any = HomeDestination
}
