package com.aiassistant.features.home.data.mapper

import com.aiassistant.common.network.HealthDto
import com.aiassistant.features.home.domain.SystemHealthResult

object SystemDataMapper {

    fun HealthDto.toDomain(): SystemHealthResult {
        return if (status.equals("ok", ignoreCase = true)) {
            SystemHealthResult.Healthy(version)
        } else {
            SystemHealthResult.Unavailable
        }
    }
}
