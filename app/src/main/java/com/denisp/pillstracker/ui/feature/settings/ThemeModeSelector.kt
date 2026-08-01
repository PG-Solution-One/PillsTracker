package com.denisp.pillstracker.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.ui.theme.AppSpacing

@Composable
internal fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
    ) {
        ThemeMode.entries.forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 64.dp),
                label = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.Xs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs),
                    ) {
                        Icon(
                            imageVector = mode.icon(),
                            contentDescription = null,
                        )
                        Text(
                            text = mode.title,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                },
            )
        }
    }
}

private fun ThemeMode.icon(): ImageVector = when (this) {
    ThemeMode.SYSTEM -> Icons.Rounded.SettingsBrightness
    ThemeMode.LIGHT -> Icons.Rounded.LightMode
    ThemeMode.DARK -> Icons.Rounded.DarkMode
}
