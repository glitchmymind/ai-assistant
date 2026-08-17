package com.aiassistant.features.home.domain

import com.aiassistant.common.core.SystemHealth

class CheckHealthUseCase(
    private val systemRepository: SystemRepository,
) {
    suspend operator fun invoke(): SystemHealth = systemRepository.checkHealth()
}
