package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.IntakeStatus

@Composable
fun IntakeStatusControls(
    status: IntakeStatus,
    enabled: Boolean = true,
    takenEnabled: Boolean = enabled,
    onStatus: (IntakeStatus) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(Modifier.padding(2.dp)) {
            StatusIconButton(
                selected = status == IntakeStatus.TAKEN,
                enabled = takenEnabled,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                contentDescription = if (status == IntakeStatus.TAKEN) "Принято" else "Отметить как принято",
                onClick = { if (status != IntakeStatus.TAKEN) onStatus(IntakeStatus.TAKEN) },
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
            }
            StatusIconButton(
                selected = status == IntakeStatus.SKIPPED,
                enabled = enabled,
                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                selectedContentColor = MaterialTheme.colorScheme.onErrorContainer,
                contentDescription = if (status == IntakeStatus.SKIPPED) "Пропущено" else "Отметить как пропущено",
                onClick = { if (status != IntakeStatus.SKIPPED) onStatus(IntakeStatus.SKIPPED) },
            ) {
                Icon(Icons.Rounded.Close, contentDescription = null)
            }
        }
    }
}

@Composable
private fun StatusIconButton(
    selected: Boolean,
    enabled: Boolean,
    selectedContainerColor: Color,
    selectedContentColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val indicatorColor = if (selected) selectedContainerColor else Color.Transparent
    val contentColor = if (selected) {
        selectedContentColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = contentColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        modifier = Modifier
            .size(48.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(indicatorColor, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) { content() }
        }
    }
}
