package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.InterfaceMode
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.ui.theme.AppStatusColors
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.LocalAppUiMetrics
import com.denisp.pillstracker.ui.theme.LocalInterfaceMode

@Composable
fun IntakeStatusControls(
    status: IntakeStatus,
    enabled: Boolean = true,
    takenEnabled: Boolean = enabled,
    subjectName: String? = null,
    modifier: Modifier = Modifier,
    onStatus: (IntakeStatus) -> Unit,
) {
    val simplified = LocalInterfaceMode.current == InterfaceMode.SIMPLIFIED
    if (simplified) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
        ) {
            StatusTextButton(
                label = if (status == IntakeStatus.TAKEN) "Отменить" else "Принято",
                selected = status == IntakeStatus.TAKEN,
                enabled = takenEnabled,
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                contentDescription = statusActionDescription(
                    action = if (status == IntakeStatus.TAKEN) {
                        "Вернуть в ожидающие"
                    } else {
                        "Отметить как принято"
                    },
                    subjectName = subjectName,
                ),
                onClick = {
                    onStatus(
                        if (status == IntakeStatus.TAKEN) IntakeStatus.PENDING else IntakeStatus.TAKEN,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
            }
            StatusTextButton(
                label = if (status == IntakeStatus.SKIPPED) "Отменить" else "Пропустить",
                selected = status == IntakeStatus.SKIPPED,
                enabled = enabled,
                selectedContainerColor = MaterialTheme.colorScheme.error,
                selectedContentColor = MaterialTheme.colorScheme.onError,
                contentDescription = statusActionDescription(
                    action = if (status == IntakeStatus.SKIPPED) {
                        "Вернуть в ожидающие"
                    } else {
                        "Отметить как пропущено"
                    },
                    subjectName = subjectName,
                ),
                onClick = {
                    onStatus(
                        if (status == IntakeStatus.SKIPPED) IntakeStatus.PENDING else IntakeStatus.SKIPPED,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Close, contentDescription = null)
            }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Xs),
        ) {
            StatusIconButton(
                selected = status == IntakeStatus.TAKEN,
                enabled = takenEnabled,
                selectedContainerColor = AppStatusColors.Taken,
                selectedContentColor = Color.White,
                contentDescription = statusActionDescription(
                    action = if (status == IntakeStatus.TAKEN) {
                        "Вернуть в ожидающие"
                    } else {
                        "Отметить как принято"
                    },
                    subjectName = subjectName,
                ),
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
                contentDescription = statusActionDescription(
                    action = if (status == IntakeStatus.SKIPPED) {
                        "Вернуть в ожидающие"
                    } else {
                        "Отметить как пропущено"
                    },
                    subjectName = subjectName,
                ),
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
}

private fun statusActionDescription(action: String, subjectName: String?): String =
    subjectName?.let { "$action: $it" } ?: action

@Composable
private fun StatusTextButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    selectedContainerColor: Color,
    selectedContentColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val uiMetrics = LocalAppUiMetrics.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = uiMetrics.iconButtonSize)
            .semantics { this.contentDescription = contentDescription },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                selectedContainerColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = if (selected) {
                selectedContentColor
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        icon()
        Text(text = label, modifier = Modifier.padding(start = AppSpacing.Xs))
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
    val uiMetrics = LocalAppUiMetrics.current
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
            .size(uiMetrics.iconButtonSize)
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
