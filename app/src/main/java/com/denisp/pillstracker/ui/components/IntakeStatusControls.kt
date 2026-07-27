package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
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
import com.denisp.pillstracker.ui.theme.AppStatusColors

@Composable
fun IntakeStatusControls(
    status: IntakeStatus,
    enabled: Boolean = true,
    takenEnabled: Boolean = enabled,
    onStatus: (IntakeStatus) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusIconButton(
                selected = status == IntakeStatus.TAKEN,
                enabled = takenEnabled,
                selectedContainerColor = AppStatusColors.Taken,
                selectedContentColor = Color.White,
                contentDescription = if (status == IntakeStatus.TAKEN) {
                    "Вернуть в ожидающие"
                } else {
                    "Отметить как принято"
                },
                onClick = {
                    onStatus(
                        if (status == IntakeStatus.TAKEN) IntakeStatus.PENDING else IntakeStatus.TAKEN,
                    )
                },
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
            }
            StatusIconButton(
                selected = status == IntakeStatus.SKIPPED,
                enabled = enabled,
                selectedContainerColor = AppStatusColors.Skipped,
                selectedContentColor = Color.White,
                contentDescription = if (status == IntakeStatus.SKIPPED) {
                    "Вернуть в ожидающие"
                } else {
                    "Отметить как пропущено"
                },
                onClick = {
                    onStatus(
                        if (status == IntakeStatus.SKIPPED) IntakeStatus.PENDING else IntakeStatus.SKIPPED,
                    )
                },
            ) {
                Icon(Icons.Rounded.Close, contentDescription = null)
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
    val indicatorColor = if (selected) {
        selectedContainerColor
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
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
            .size(36.dp)
            .background(indicatorColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) { content() }
        }
    }
}
