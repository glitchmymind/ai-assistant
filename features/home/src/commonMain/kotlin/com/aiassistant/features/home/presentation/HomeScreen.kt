package com.aiassistant.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.aiassistant.features.conversation.presentation.ConversationSection
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = AppConstants.APP_NAME,
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Check API availability and try conversations.",
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

                state.health
                    .onLoading { LoadingContent() }
                    .onFailure {
                        StatusBadge(
                            text = "Unavailable",
                            isPositive = false,
                        )
                        Text(
                            text = "Version: —",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    .onSuccess { available ->
                        StatusBadge(
                            text = "Available",
                            isPositive = true,
                        )
                        Text(
                            text = "Version: ${available.version}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
            }
        }

        AppButton(
            text = if (state.health is HealthUiState.Loading) "Checking..." else "Refresh health",
            onClick = { viewModel.onAction(HomeUiAction.Refresh) },
            enabled = state.health !is HealthUiState.Loading,
            modifier = Modifier.align(Alignment.Start),
        )

        ConversationSection(
            title = state.title,
            conversationId = state.conversationId,
            state = state.conversation,
            onTitleChange = { viewModel.onAction(HomeUiAction.TitleChanged(it)) },
            onIdChange = { viewModel.onAction(HomeUiAction.ConversationIdChanged(it)) },
            onCreate = { viewModel.onAction(HomeUiAction.CreateConversation) },
            onLoad = { viewModel.onAction(HomeUiAction.LoadConversation) },
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
    }
}
