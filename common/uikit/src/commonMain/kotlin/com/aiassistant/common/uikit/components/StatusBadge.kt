package com.aiassistant.common.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aiassistant.common.uikit.theme.Spacing
import com.aiassistant.common.uikit.theme.SuccessDark
import com.aiassistant.common.uikit.theme.SuccessLight

@Composable
fun StatusBadge(
    text: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
) {
    val background = if (isPositive) {
        SuccessLight.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
    }
    val content = if (isPositive) SuccessDark else MaterialTheme.colorScheme.error

    Text(
        text = text,
        color = content,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
    )
}
