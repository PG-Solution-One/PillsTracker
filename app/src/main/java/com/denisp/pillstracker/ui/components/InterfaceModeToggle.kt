package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.InterfaceMode
import com.denisp.pillstracker.ui.theme.AppSpacing

@Composable
fun InterfaceModeToggle(
    interfaceMode: InterfaceMode,
    onInterfaceModeChanged: (InterfaceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val simplified = interfaceMode == InterfaceMode.SIMPLIFIED
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interface_mode_toggle")
            .heightIn(min = 64.dp)
            .toggleable(
                value = simplified,
                role = Role.Switch,
                onValueChange = { enabled ->
                    onInterfaceModeChanged(
                        if (enabled) InterfaceMode.SIMPLIFIED else InterfaceMode.STANDARD,
                    )
                },
            )
            .semantics { contentDescription = "Упрощённый интерфейс" }
            .padding(vertical = AppSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs),
        ) {
            Text(
                text = "Упрощённый интерфейс",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Крупный текст, большие кнопки и понятные действия",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = simplified,
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}
