package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.ui.MedicineBackgroundPalette
import com.denisp.pillstracker.ui.MedicinePalette
import com.denisp.pillstracker.ui.components.MedicineFormSticker
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppTextField
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
internal fun BasicMedicineStep(
    name: String,
    onNameChanged: (String) -> Unit,
    form: MedicineForm,
    onFormChanged: (MedicineForm) -> Unit,
    pillShape: PillShape,
    onPillShapeChanged: (PillShape) -> Unit,
    colorArgb: Long,
    onColorChanged: (Long) -> Unit,
    secondaryColorArgb: Long?,
    onSecondaryColorChanged: (Long?) -> Unit,
    backgroundColorArgb: Long,
    onBackgroundColorChanged: (Long) -> Unit,
    showError: Boolean,
) {
    EditorStepContent(
        modifier = Modifier.animateContentSize(animationSpec = tween(durationMillis = 180)),
    ) {
        AppTextField(
            value = name,
            onValueChange = onNameChanged,
            label = "Название лекарства",
            placeholder = "Например, Витамин D",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
            ),
            isError = showError && name.isBlank(),
            supportingText = if (showError && name.isBlank()) {
                { Text("Введите название") }
            } else {
                null
            },
        )

        EditorSectionCard(
            title = "Форма лекарства",
            supportingText = "Листайте влево или вправо — карусель не заканчивается",
        ) {
            InfiniteMedicineFormPager(
                selected = form,
                pillShape = pillShape,
                primaryColorArgb = colorArgb,
                secondaryColorArgb = secondaryColorArgb,
                backgroundColorArgb = backgroundColorArgb,
                onSelected = onFormChanged,
            )
        }

        if (form == MedicineForm.TABLET) {
            EditorSectionCard(title = "Форма таблетки") {
                SelectionField(
                    label = "Выберите внешний вид",
                    selected = pillShape,
                    options = PillShape.entries.filter { it != PillShape.CAPSULE },
                    onSelected = onPillShapeChanged,
                    title = PillShape::title,
                )
            }
        }

        EditorSectionCard(
            title = "Оформление",
            supportingText = "Цвета сразу отображаются в карусели",
        ) {
            if (form == MedicineForm.TABLET || form == MedicineForm.CAPSULE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            "Два цвета",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Для капсул и двухцветных таблеток",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = secondaryColorArgb != null,
                        onCheckedChange = {
                            onSecondaryColorChanged(if (it) MedicinePalette[1] else null)
                        },
                    )
                }
            }

            MedicineColorPicker(
                title = if (secondaryColorArgb == null) {
                    "Цвет лекарства"
                } else {
                    "Первая половина"
                },
                selectedColor = colorArgb,
                onColorChanged = onColorChanged,
            )
            secondaryColorArgb?.let { secondaryColor ->
                MedicineColorPicker(
                    title = "Вторая половина",
                    selectedColor = secondaryColor,
                    onColorChanged = { onSecondaryColorChanged(it) },
                )
            }
            MedicineColorPicker(
                title = "Фон стикера",
                selectedColor = backgroundColorArgb,
                onColorChanged = onBackgroundColorChanged,
                colors = MedicineBackgroundPalette,
            )
        }
    }
}

@Composable
private fun InfiniteMedicineFormPager(
    selected: MedicineForm,
    pillShape: PillShape,
    primaryColorArgb: Long,
    secondaryColorArgb: Long?,
    backgroundColorArgb: Long,
    onSelected: (MedicineForm) -> Unit,
) {
    val options = MedicineForm.entries
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = initialMedicineFormPage(selectedIndex, options.size),
        pageCount = { Int.MAX_VALUE },
    )
    val scope = rememberCoroutineScope()
    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelected by rememberUpdatedState(onSelected)
    val previewIndex by remember {
        derivedStateOf {
            medicineFormIndexForPage(pagerState.currentPage, options.size)
        }
    }

    LaunchedEffect(pagerState, options) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                val settledForm = options[
                    medicineFormIndexForPage(settledPage, options.size)
                ]
                if (settledForm != currentSelected) currentOnSelected(settledForm)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 54.dp),
            pageSpacing = AppSpacing.Md,
        ) { page ->
            val pageForm = options[medicineFormIndexForPage(page, options.size)]

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val pageOffset = (
                            (pagerState.currentPage - page) +
                                pagerState.currentPageOffsetFraction
                            ).absoluteValue.coerceIn(0f, 1f)
                        alpha = 1f - pageOffset * 0.48f
                        scaleX = 1f - pageOffset * 0.14f
                        scaleY = 1f - pageOffset * 0.14f
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
            ) {
                MedicineFormSticker(
                    form = pageForm,
                    shape = pillShape,
                    primaryColorArgb = primaryColorArgb,
                    secondaryColorArgb = secondaryColorArgb.takeIf {
                        pageForm == MedicineForm.TABLET || pageForm == MedicineForm.CAPSULE
                    },
                    size = 120.dp,
                    backgroundColorArgb = backgroundColorArgb,
                )
                Text(
                    text = pageForm.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        FilledTonalIconButton(
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.settledPage - 1)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Предыдущая форма",
            )
        }
        FilledTonalIconButton(
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.settledPage + 1)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Следующая форма",
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.indices.forEach { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (index == previewIndex) 8.dp else 6.dp)
                    .background(
                        color = if (index == previewIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun MedicineColorPicker(
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
