package com.aiassistant.features.conversation.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aiassistant.common.uikit.components.AppButton
import com.aiassistant.common.uikit.components.LoadingContent
import com.aiassistant.common.uikit.components.StatusBadge
import com.aiassistant.common.uikit.theme.Spacing
import com.aiassistant.features.conversation.domain.model.ConversationError
import com.aiassistant.features.conversation.presentation.model.mvi.ConversationUiState

@Composable
fun ConversationSection(
    title: String,
    conversationId: String,
    state: ConversationUiState,
    onTitleChange: (String) -> Unit,
    onIdChange: (String) -> Unit,
    onCreate: () -> Unit,
    onLoad: () -> Unit,
) {
    val isBusy = state is ConversationUiState.Loading

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = "Conversations",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Create a conversation or look one up by id.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true,
                enabled = !isBusy,
            )
            AppButton(
                text = "Create",
                onClick = onCreate,
                enabled = !isBusy && title.isNotBlank(),
            )

            OutlinedTextField(
                value = conversationId,
                onValueChange = onIdChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Conversation id") },
                singleLine = true,
                enabled = !isBusy,
            )
            AppButton(
                text = "Load",
                onClick = onLoad,
                enabled = !isBusy && conversationId.isNotBlank(),
            )

            when (val current = state) {
                ConversationUiState.Idle -> Unit
                ConversationUiState.Loading -> LoadingContent()
                is ConversationUiState.Loaded -> {
                    StatusBadge(text = "Loaded", isPositive = true)
                    Text(
                        text = "Id: ${current.conversation.id}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Title: ${current.conversation.title}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Created: ${current.conversation.createdAt}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                is ConversationUiState.Failed -> {
                    StatusBadge(text = "Failed", isPositive = false)
                    Text(
                        text = current.error.toMessage(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

private fun ConversationError.toMessage(): String = when (this) {
    ConversationError.MalformedId -> "Malformed UUID"
    ConversationError.NotFound -> "Conversation doesn't exist"
    ConversationError.InvalidTitle -> "Invalid title"
    ConversationError.Server -> "Server error"
    ConversationError.Network -> "Network error"
}
