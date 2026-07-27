package com.denisp.pillstracker.ui.feature.history

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.domain.IntakeRules
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.TrackerSnapshot
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.FullDateFormatter
import com.denisp.pillstracker.ui.RussianLocale
import com.denisp.pillstracker.ui.asTime
import com.denisp.pillstracker.ui.components.IntakeStatusControls
import com.denisp.pillstracker.ui.components.MedicineAppearance
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import kotlin.math.roundToInt

private val MonthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", RussianLocale)

@Composable
fun HistoryScreen(
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMonthCalendar by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val doses = historyDosesForDate(snapshot, repository, selectedDate)
    val canEditSelectedDate = !selectedDate.isAfter(today)

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 40.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    "История",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { selectedDate = selectedDate.minusWeeks(1) }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Предыдущая неделя",
                        )
                    }
                    TextButton(onClick = { showMonthCalendar = true }) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            selectedDate.format(FullDateFormatter).replaceFirstChar { it.uppercase() },
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    IconButton(onClick = { selectedDate = selectedDate.plusWeeks(1) }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Следующая неделя",
                        )
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    (0L..6L).forEach { offset ->
                        val date = startOfWeek.plusDays(offset)
                        val dayDoses = historyDosesForDate(snapshot, repository, date)
                        DayCell(
                            modifier = Modifier.weight(1f),
                            date = date,
                            selected = date == selectedDate,
                            hasTaken = dayDoses.any { it.status == IntakeStatus.TAKEN },
                            hasSkipped = dayDoses.any { it.status == IntakeStatus.SKIPPED },
                            onClick = { selectedDate = date },
                        )
                    }
                }
            }
            item {
                val taken = doses.count { it.status == IntakeStatus.TAKEN }
                val skipped = doses.count { it.status == IntakeStatus.SKIPPED }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryBadge(
                        text = "Принято: $taken",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    SummaryBadge(
                        text = "Пропущено: $skipped",
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            if (doses.isNotEmpty()) {
                item {
                    Text(
                        text = if (canEditSelectedDate) {
                            "Свайп вправо — принято, влево — пропущено"
                        } else {
                            "Будущие даты доступны только для просмотра"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (doses.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(20.dp)) {
                        Text(
                            "На этот день записей нет",
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(doses, key = { "${it.medicine.id}-${it.scheduledAt}" }) { dose ->
                    HistoryDoseCard(
                        dose = dose,
                        canEdit = canEditSelectedDate,
                        onStatus = {
                            repository.markIntake(dose.medicine.id, dose.scheduledAt, it)
                        },
                    )
                }
            }
        }
    }

    if (showMonthCalendar) {
        MonthCalendarDialog(
            selectedDate = selectedDate,
            snapshot = snapshot,
            repository = repository,
            onDateSelected = {
                selectedDate = it
                showMonthCalendar = false
            },
            onDismiss = { showMonthCalendar = false },
        )
    }
}

private fun historyDosesForDate(
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
    date: LocalDate,
): List<ScheduledDose> {
    val scheduledDoses = repository.dosesForDate(date, activeOnly = false)
    val scheduledKeys = scheduledDoses.map { it.medicine.id to it.scheduledAt }.toSet()
    val manualDoses = snapshot.records
        .asSequence()
        .filter { record ->
            val recordDate = Instant.ofEpochMilli(record.scheduledAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            recordDate == date && (record.medicineId to record.scheduledAt) !in scheduledKeys
        }
        .mapNotNull { record ->
            snapshot.medicines.firstOrNull { it.id == record.medicineId }?.let { medicine ->
                ScheduledDose(medicine, record.scheduledAt, record.status, record.updatedAt)
            }
        }
        .toList()
    return (scheduledDoses + manualDoses).sortedBy { it.scheduledAt }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    date: LocalDate,
    selected: Boolean,
    hasTaken: Boolean,
    hasSkipped: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    Column(
        modifier = modifier
            .background(background, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, RussianLocale).take(2),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(date.dayOfMonth.toString(), fontWeight = FontWeight.SemiBold)
        StatusDots(hasTaken = hasTaken, hasSkipped = hasSkipped)
    }
}

@Composable
private fun HistoryDoseCard(
    dose: ScheduledDose,
    canEdit: Boolean,
    onStatus: (IntakeStatus) -> Unit,
) {
    val canTake = canEdit && IntakeRules.canMarkTaken(
        remaining = dose.medicine.remaining,
        tabletsPerIntake = dose.medicine.tabletsPerIntake,
        currentStatus = dose.status,
    )
    val currentOnStatus by rememberUpdatedState(onStatus)
    val currentCanEdit by rememberUpdatedState(canEdit)
    val currentCanTake by rememberUpdatedState(canTake)
    val density = LocalDensity.current
    val thresholdPx = with(density) { 72.dp.toPx() }
    val maximumOffsetPx = with(density) { 180.dp.toPx() }
    var dragOffsetPx by remember(dose.medicine.id, dose.scheduledAt) {
        mutableFloatStateOf(0f)
    }
    val dragState = rememberDraggableState { delta ->
        val minimum = if (currentCanEdit) -maximumOffsetPx else 0f
        val maximum = if (currentCanTake) maximumOffsetPx else 0f
        dragOffsetPx = (dragOffsetPx + delta).coerceIn(minimum, maximum)
    }
    val cardShape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape),
    ) {
        if (dragOffsetPx != 0f) {
            val isTaken = dragOffsetPx > 0f
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        if (isTaken) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                        RoundedCornerShape(18.dp),
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = if (isTaken) Alignment.CenterStart else Alignment.CenterEnd,
            ) {
                Text(
                    if (isTaken) "Принято ✓" else "Пропущено ✕",
                    fontWeight = FontWeight.Bold,
                    color = if (isTaken) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(dragOffsetPx.roundToInt(), 0) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = canEdit,
                    onDragStopped = {
                        val selectedStatus = when {
                            dragOffsetPx >= thresholdPx && currentCanTake -> IntakeStatus.TAKEN
                            dragOffsetPx <= -thresholdPx && currentCanEdit -> IntakeStatus.SKIPPED
                            else -> null
                        }
                        val returnAnimation = Animatable(dragOffsetPx)
                        returnAnimation.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        ) {
                            dragOffsetPx = value
                        }
                        dragOffsetPx = 0f
                        selectedStatus?.let(currentOnStatus)
                    },
                ),
            shape = cardShape,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MedicineAppearance(medicine = dose.medicine, size = 28.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(
                            dose.medicine.name,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${dose.scheduledAt.asTime()} · ${dose.medicine.dosage}" +
                                if (dose.medicine.scheduleKind == ScheduleKind.AS_NEEDED) {
                                    " · по необходимости"
                                } else {
                                    ""
                                },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (dose.updatedAt != null && dose.status != IntakeStatus.PENDING) {
                            Text(
                                text = if (dose.status == IntakeStatus.TAKEN) {
                                    "Принято в ${dose.updatedAt.asTime()}"
                                } else {
                                    "Отмечено в ${dose.updatedAt.asTime()}"
                                },
                                color = if (dose.status == IntakeStatus.TAKEN) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    IntakeStatusControls(
                        status = dose.status,
                        enabled = canEdit,
                        takenEnabled = canTake,
                        onStatus = onStatus,
                    )
                }
                if (canEdit && !canTake && dose.status != IntakeStatus.TAKEN) {
                    Text(
                        "Недостаточно лекарства: осталось ${dose.medicine.remaining.displayAmount()} шт.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarDialog(
    selectedDate: LocalDate,
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var visibleMonth by remember(selectedDate) { mutableStateOf(YearMonth.from(selectedDate)) }
    val firstDay = visibleMonth.atDay(1)
    val gridStart = firstDay.minusDays((firstDay.dayOfWeek.value - 1).toLong())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { visibleMonth = visibleMonth.minusMonths(1) }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Предыдущий месяц",
                    )
                }
                Text(
                    visibleMonth.format(MonthFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = { visibleMonth = visibleMonth.plusMonths(1) }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Следующий месяц",
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    DayOfWeek.entries.forEach { day ->
                        Text(
                            day.getDisplayName(TextStyle.SHORT, RussianLocale).take(2),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                repeat(6) { week ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { day ->
                            val date = gridStart.plusDays((week * 7 + day).toLong())
                            val dayDoses = historyDosesForDate(snapshot, repository, date)
                            CalendarDayCell(
                                modifier = Modifier.weight(1f),
                                date = date,
                                inCurrentMonth = YearMonth.from(date) == visibleMonth,
                                selected = date == selectedDate,
                                hasTaken = dayDoses.any { it.status == IntakeStatus.TAKEN },
                                hasSkipped = dayDoses.any { it.status == IntakeStatus.SKIPPED },
                                onClick = { onDateSelected(date) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDateSelected(LocalDate.now()) }) {
                Text("Сегодня")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        },
    )
}

@Composable
private fun CalendarDayCell(
    modifier: Modifier = Modifier,
    date: LocalDate,
    inCurrentMonth: Boolean,
    selected: Boolean,
    hasTaken: Boolean,
    hasSkipped: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val selectedBorder = if (selected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(0.dp, Color.Transparent)
    }
    Column(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(0.9f)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape,
            )
            .border(selectedBorder, shape)
            .clickable(onClick = onClick)
            .alpha(if (inCurrentMonth) 1f else 0.38f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            date.dayOfMonth.toString(),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
        StatusDots(hasTaken = hasTaken, hasSkipped = hasSkipped)
    }
}

@Composable
private fun StatusDots(
    hasTaken: Boolean,
    hasSkipped: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.size(width = 12.dp, height = 6.dp),
    ) {
        if (hasTaken) {
            Box(
                Modifier
                    .size(5.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
        if (hasSkipped) {
            Box(
                Modifier
                    .size(5.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape),
            )
        }
    }
}

@Composable
private fun SummaryBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(color = containerColor, contentColor = contentColor, shape = CircleShape) {
        Text(
            text,
            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
