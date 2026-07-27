package com.denisp.pillstracker.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.R

internal enum class MainSection(val title: String) {
    TODAY("Главная"),
    MEDICINES("Лекарства"),
    HISTORY("История"),
    SETTINGS("Настройки"),
}

@Composable
internal fun MainNavigationBar(
    selectedSection: MainSection,
    onSectionSelected: (MainSection) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 4.dp,
    ) {
        MainSection.entries.forEach { item ->
            NavigationBarItem(
                selected = selectedSection == item,
                onClick = { onSectionSelected(item) },
                icon = {
                    when (item) {
                        MainSection.MEDICINES -> Icon(
                            painter = painterResource(R.drawable.medicine_form_capsule),
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(27.dp)
                                .rotate(-32f),
                        )

                        MainSection.TODAY -> MainSectionIcon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = item.title,
                        )

                        MainSection.HISTORY -> MainSectionIcon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = item.title,
                        )

                        MainSection.SETTINGS -> MainSectionIcon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = item.title,
                        )
                    }
                },
                label = { Text(item.title) },
            )
        }
    }
}

@Composable
private fun MainSectionIcon(
    imageVector: ImageVector,
    contentDescription: String,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(28.dp),
    )
}
