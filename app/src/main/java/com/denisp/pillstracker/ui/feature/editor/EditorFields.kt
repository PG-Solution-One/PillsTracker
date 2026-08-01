package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.ui.theme.AppSectionHeader
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard

@Composable
internal fun EditorStepContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
        content = content,
    )
}

@Composable
internal fun EditorSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppSurfaceCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
        ) {
            AppSectionHeader(
                title = title,
                supportingText = supportingText,
            )
            content()
        }
    }
}

@Composable
internal fun <T> SelectionField(
    label: String,
    selected: T,
    options: List<T>,
    onSelected: (T) -> Unit,
    title: (T) -> String,
    modifier: Modifier = Modifier,
    columns: Int = 2,
) {
    require(columns > 0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm)) {
            options.chunked(columns).forEach { rowOptions ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
                ) {
                    rowOptions.forEach { option ->
                        FilterChip(
                            selected = option == selected,
                            onClick = { onSelected(option) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .heightIn(min = 48.dp)
                                .testTag("selection-option-${title(option)}"),
                            label = {
                                Text(
                                    text = title(option),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                )
                            },
                        )
                    }
                    repeat(columns - rowOptions.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
