package com.aiassistant.features.home.data.mapper

import com.aiassistant.common.core.SystemHealth
import com.aiassistant.common.network.HealthDto

object SystemDataMapper {

    fun HealthDto.toDomain() = SystemHealth(
        isAvailable = status.equals("ok", ignoreCase = true),
        version = version,
    )
}
