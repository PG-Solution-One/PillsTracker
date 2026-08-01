package com.denisp.pillstracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.ui.theme.LocalAppUiMetrics

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
                        MainSection.MEDICINES -> MedicineNavigationIcon(item.title)

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
private fun MedicineNavigationIcon(contentDescription: String) {
    val color = LocalContentColor.current
    val iconSize = LocalAppUiMetrics.current.navigationIconSize
    Canvas(
        modifier = Modifier
            .size(iconSize)
            .rotate(-32f)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val strokeWidth = size.minDimension * 0.08f
        val capsuleWidth = size.width * 0.89f
        val capsuleHeight = size.height * 0.46f
        val left = (size.width - capsuleWidth) / 2f
        val top = (size.height - capsuleHeight) / 2f

        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(capsuleWidth, capsuleHeight),
            cornerRadius = CornerRadius(capsuleHeight / 2f),
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = color,
            start = Offset(size.width / 2f, top + strokeWidth / 2f),
            end = Offset(size.width / 2f, top + capsuleHeight - strokeWidth / 2f),
            strokeWidth = strokeWidth,
        )
    }
}

@Composable
private fun MainSectionIcon(
    imageVector: ImageVector,
    contentDescription: String,
) {
    val iconSize = LocalAppUiMetrics.current.navigationIconSize
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(iconSize),
    )
}
