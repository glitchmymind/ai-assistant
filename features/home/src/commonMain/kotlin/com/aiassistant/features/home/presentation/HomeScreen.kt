package com.aiassistant.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiassistant.common.core.AppConstants
import com.aiassistant.common.uikit.components.AppButton
import com.aiassistant.common.uikit.components.LoadingContent
import com.aiassistant.common.uikit.components.StatusBadge
import com.aiassistant.common.uikit.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = AppConstants.APP_NAME,
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Check API availability and service version.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Text(
                    text = "System health",
                    style = MaterialTheme.typography.titleLarge,
                )

                when {
                    uiState.isLoading && uiState.health == null -> LoadingContent()
                    else -> {
                        val health = uiState.health
                        StatusBadge(
                            text = if (health?.isAvailable == true) "Available" else "Unavailable",
                            isPositive = health?.isAvailable == true,
                        )
                        Text(
                            text = "Version: ${health?.version ?: "—"}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        uiState.errorMessage?.let { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))
        AppButton(
            text = if (uiState.isLoading) "Checking..." else "Refresh health",
            onClick = { viewModel.onAction(HomeUiAction.Refresh) },
            enabled = !uiState.isLoading,
            modifier = Modifier.align(Alignment.Start),
        )
    }
}
