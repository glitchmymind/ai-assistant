package com.aiassistant.features.conversation.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.aiassistant.features.conversation.domain.model.Conversation
import com.aiassistant.features.conversation.domain.model.ConversationError
import com.aiassistant.features.conversation.presentation.model.mvi.ConversationUiState

@Composable
fun ConversationSection(
    title: String,
    conversationId: String,
    state: ConversationUiState,
    conversations: List<Conversation>,
    conversationsLoaded: Boolean,
    onTitleChange: (String) -> Unit,
    onIdChange: (String) -> Unit,
    onCreate: () -> Unit,
    onLoad: () -> Unit,
    onLoadAll: () -> Unit,
    onUpdate: () -> Unit,
    onSelectConversation: (Conversation) -> Unit,
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
                text = "Create, load one by id, update a title, or load all conversations.",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                AppButton(
                    text = "Load",
                    onClick = onLoad,
                    enabled = !isBusy && conversationId.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Update",
                    onClick = onUpdate,
                    enabled = !isBusy && conversationId.isNotBlank() && title.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
            AppButton(
                text = "Load all",
                onClick = onLoadAll,
                enabled = !isBusy,
            )

            if (conversationsLoaded || conversations.isNotEmpty()) {
                Text(
                    text = "All conversations",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (conversations.isEmpty()) {
                    Text(
                        text = "No conversations yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                } else {
                    conversations.forEach { conversation ->
                        ConversationListItem(
                            conversation = conversation,
                            selected = conversation.id == conversationId,
                            enabled = !isBusy,
                            onClick = { onSelectConversation(conversation) },
                        )
                    }
                }
            }

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

@Composable
private fun ConversationListItem(
    conversation: Conversation,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = conversation.title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = conversation.id,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            if (selected) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun ConversationError.toMessage(): String = when (this) {
    ConversationError.MalformedId -> "Malformed UUID"
    ConversationError.NotFound -> "Conversation doesn't exist"
    ConversationError.InvalidTitle -> "Invalid title"
    ConversationError.MissingIdempotencyKey -> "Missing Idempotency-Key"
    ConversationError.MalformedIdempotencyKey -> "Malformed Idempotency-Key"
    ConversationError.Server -> "Server error"
    ConversationError.Network -> "Network error"
}
