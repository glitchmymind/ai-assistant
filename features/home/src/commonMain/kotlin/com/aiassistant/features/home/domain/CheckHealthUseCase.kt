package com.aiassistant.features.home.domain

class CheckHealthUseCase(
    private val systemRepository: SystemRepository,
) {
    suspend operator fun invoke(): SystemHealthResult = systemRepository.checkHealth()
}
