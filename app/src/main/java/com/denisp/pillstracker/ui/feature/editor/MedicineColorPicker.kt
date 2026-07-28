package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.ui.MedicinePalette
import com.denisp.pillstracker.ui.theme.AppSpacing

@Composable
internal fun MedicineColorPicker(
    title: String,
    selectedColor: Long,
    onColorChanged: (Long) -> Unit,
    colors: List<Long> = MedicinePalette,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (color == selectedColor) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            Color.Transparent
                        },
                        CircleShape,
                    )
                    .padding(3.dp)
                    .background(Color(color.toInt()), CircleShape)
                    .border(
                        width = 1.dp,
                        color = if (color == MedicinePalette.first()) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            Color.Transparent
                        },
                        shape = CircleShape,
                    )
                    .clickable { onColorChanged(color) },
            )
        }
    }
}
