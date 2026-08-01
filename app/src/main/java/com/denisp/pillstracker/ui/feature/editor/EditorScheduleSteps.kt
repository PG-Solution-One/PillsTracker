package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.dayMask
import com.denisp.pillstracker.ui.DateWithYearFormatter
import com.denisp.pillstracker.ui.TimeFormatter
import com.denisp.pillstracker.ui.theme.AppPickerField
import com.denisp.pillstracker.ui.theme.AppSecondaryButton
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import com.denisp.pillstracker.ui.theme.AppTextField
import java.time.LocalDate
import java.time.LocalTime

@Composable
internal fun CourseStep(
    startDate: LocalDate,
    onPickStartDate: () -> Unit,
    endMode: CourseEndMode,
    onEndModeChanged: (CourseEndMode) -> Unit,
    endDate: LocalDate,
    onPickEndDate: () -> Unit,
    courseDays: String,
    onCourseDaysChanged: (String) -> Unit,
    scheduleKind: ScheduleKind,
    onScheduleKindChanged: (ScheduleKind) -> Unit,
    showError: Boolean,
) {
    EditorStepContent {
        EditorSectionCard(
            title = "Период курса",
            supportingText = "Выберите начало и продолжительность лечения",
        ) {
            DateButton("Дата начала", startDate, onPickStartDate)
            SelectionField(
                label = "Продолжительность курса",
                selected = endMode,
                options = CourseEndMode.entries,
                onSelected = onEndModeChanged,
                title = CourseEndMode::title,
            )
            when (endMode) {
                CourseEndMode.WITHOUT_END -> Unit
                CourseEndMode.END_DATE -> DateButton(
                    "Дата окончания",
                    endDate,
                    onPickEndDate,
                )

                CourseEndMode.DAYS_COUNT -> {
                    val invalid = (courseDays.toLongOrNull() ?: 0) <= 0
                    AppTextField(
                        value = courseDays,
                        onValueChange = onCourseDaysChanged,
                        label = "Количество дней",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = showError && invalid,
                        supportingText = if (showError && invalid) {
                            { Text("Введите количество дней больше нуля") }
                        } else {
                            null
                        },
                    )
                }
            }
        }

        EditorSectionCard(
            title = "Схема приёма",
            supportingText = "Определяет, в какие дни будут создаваться напоминания",
        ) {
            SelectionField(
                label = "Повторение",
                selected = scheduleKind,
                options = ScheduleKind.entries,
                onSelected = onScheduleKindChanged,
                title = ScheduleKind::title,
            )
            if (scheduleKind == ScheduleKind.EVERY_OTHER_DAY) {
                Text(
                    "Отсчёт «через день» начинается с даты начала курса.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun TimeStep(
    scheduleKind: ScheduleKind,
    times: List<EditableScheduleTime>,
    onAdd: () -> Unit,
    onChangeTime: (Int) -> Unit,
    onToggleDay: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    showError: Boolean,
) {
    EditorStepContent {
        if (scheduleKind == ScheduleKind.AS_NEEDED) {
            EditorSectionCard(
                title = "Без фиксированного времени",
                supportingText = "Приём можно будет отметить вручную на главном экране",
            ) {
                Text(
                    "Напоминания по расписанию для этого лекарства создаваться не будут.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            if (showError && hasOverlappingScheduleTimes(scheduleKind, times)) {
                Text(
                    "Такое время уже добавлено для тех же дней",
                    color = MaterialTheme.colorScheme.error,
                )
            }
            times.forEachIndexed { index, schedule ->
                AppSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.Xl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onChangeTime(index) }
                                    .padding(horizontal = AppSpacing.Sm, vertical = AppSpacing.Xs),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    if (times.size > 1) {
                                        "Приём ${index + 1}"
                                    } else {
                                        "Время приёма"
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Text(
                                    LocalTime.of(
                                        schedule.minuteOfDay / 60,
                                        schedule.minuteOfDay % 60,
                                    ).format(TimeFormatter),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (scheduleKind != ScheduleKind.SELECTED_DAYS) {
                                    Text(
                                        scheduleKind.title,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                            FilledTonalIconButton(
                                onClick = { onChangeTime(index) },
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Изменить время")
                            }
                            if (times.size > 1) {
                                IconButton(
                                    onClick = { onRemove(index) },
                                    modifier = Modifier.size(44.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Удалить время",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                        if (scheduleKind == ScheduleKind.SELECTED_DAYS) {
                            Text("Дни для этого времени", fontWeight = FontWeight.SemiBold)
                            DaySelector(schedule.dayMask) { day -> onToggleDay(index, day) }
                            if (showError && schedule.dayMask == 0) {
                                Text(
                                    "Выберите хотя бы один день",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
            AppSecondaryButton(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("+ Добавить время")
            }
        }
    }
}

@Composable
private fun DaySelector(mask: Int, onToggle: (Int) -> Unit) {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            val selected = mask and dayMask(index + 1) != 0
            Surface(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                shape = CircleShape,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clickable { onToggle(index + 1) },
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(vertical = 9.dp),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun DateButton(label: String, date: LocalDate, onClick: () -> Unit) {
    AppPickerField(
        label = label,
        value = date.format(DateWithYearFormatter),
        onClick = onClick,
        leadingIcon = Icons.Rounded.CalendarMonth,
    )
}
