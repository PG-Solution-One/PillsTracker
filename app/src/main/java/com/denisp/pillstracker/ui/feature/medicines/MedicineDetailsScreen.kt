package com.denisp.pillstracker.ui.feature.medicines

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduleTime
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.dayMask
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.components.MedicineAppearance
import com.denisp.pillstracker.ui.theme.AppRadii
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MedicineDetailsScreen(
    medicine: Medicine,
    todayDoses: List<ScheduledDose>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    nowMillis: Long = System.currentTimeMillis(),
) {
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
                title = { Text("О лекарстве", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Редактировать лекарство",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(
                    start = AppSpacing.Screen,
                    top = AppSpacing.Sm,
                    end = AppSpacing.Screen,
                    bottom = AppSpacing.Xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
            ) {
                item {
                    MedicineHeroCard(medicine)
                }
                item {
                    IntakeInstructionsCard(medicine)
                }
                item {
                    DetailsSectionTitle("Описание")
                    Text(
                        text = medicine.note.ifBlank { "Заметка не добавлена" },
                        modifier = Modifier.padding(top = AppSpacing.Sm),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (medicine.note.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
                item {
                    DetailsSectionTitle(
                        if (medicine.trackStock) "Курс и остаток" else "Курс",
                    )
                    AppSurfaceCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppSpacing.Sm),
                        elevated = true,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.Lg),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
                        ) {
                            if (medicine.trackStock) {
                                DetailsInfoRow(
                                    icon = Icons.Rounded.Inventory2,
                                    title = "Осталось",
                                    value = "${medicine.remaining.displayAmount()} из " +
                                        "${medicine.packageSize.displayAmount()} шт.",
                                    highlight = medicine.remaining <= medicine.tabletsPerIntake * 3,
                                )
                            }
                            DetailsInfoRow(
                                icon = Icons.Rounded.CalendarMonth,
                                title = "Курс",
                                value = medicineCourseSummary(medicine),
                            )
                        }
                    }
                }
                item {
                    DetailsSectionTitle("Расписание")
                    Text(
                        text = medicineScheduleSummary(medicine),
                        modifier = Modifier.padding(top = AppSpacing.Xs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (medicine.scheduleKind == ScheduleKind.AS_NEEDED) {
                    item {
                        AppSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Фиксированного времени нет — лекарство принимается по необходимости.",
                                modifier = Modifier.padding(AppSpacing.Lg),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                } else {
                    val dosesByMinute = todayDoses.associateBy(::minuteOfDay)
                    items(
                        items = medicine.times.sortedBy { it.minuteOfDay },
                        key = { "${it.id}:${it.minuteOfDay}:${it.dayMask}" },
                    ) { schedule ->
                        MedicineScheduleCard(
                            medicine = medicine,
                            schedule = schedule,
                            dose = dosesByMinute[schedule.minuteOfDay],
                            nowMillis = nowMillis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicineHeroCard(medicine: Medicine) {
    val background = Color(medicine.backgroundColorArgb.toInt())
    val contentColor = if (background.luminance() >= 0.48f) {
        Color(0xFF17263A)
    } else {
        Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 184.dp),
        shape = RoundedCornerShape(AppRadii.Dashboard),
        colors = CardDefaults.cardColors(
            containerColor = background,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Xl),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
            ) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    color = contentColor.copy(alpha = 0.11f),
                    contentColor = contentColor,
                    shape = RoundedCornerShape(AppRadii.Small),
                ) {
                    Text(
                        text = medicine.state.detailTitle(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = medicine.dosage,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = medicine.form.title,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            MedicineAppearance(
                medicine = medicine,
                size = 124.dp,
                showContainer = false,
            )
        }
    }
}

@Composable
private fun IntakeInstructionsCard(medicine: Medicine) {
    AppSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(AppRadii.Small),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = medicine.mealTiming.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "За один приём: ${medicine.tabletsPerIntake.displayAmount()} шт.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DetailsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun DetailsInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    highlight: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlight) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (highlight) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun MedicineScheduleCard(
    medicine: Medicine,
    schedule: ScheduleTime,
    dose: ScheduledDose?,
    nowMillis: Long,
) {
    val period = medicineDayPeriod(schedule.minuteOfDay)
    val statusLabel = medicineDoseStatusLabel(
        status = dose?.status,
        scheduledAt = dose?.scheduledAt,
        nowMillis = nowMillis,
    )
    val statusColors = when {
        dose?.status == IntakeStatus.TAKEN ->
            Color(0xFFDDF5E8) to Color(0xFF14623E)
        dose?.status == IntakeStatus.SKIPPED ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        dose == null ->
            MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
        dose.scheduledAt > nowMillis ->
            Color(0xFFDDEFF7) to Color(0xFF245C70)
        else ->
            Color(0xFFF8E4B8) to Color(0xFF6D5200)
    }

    AppSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = period.icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = period.color,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "${period.title} · ${formatMinuteOfDay(schedule.minuteOfDay)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = buildString {
                        append("${medicine.tabletsPerIntake.displayAmount()} шт. (${medicine.dosage})")
                        if (medicine.scheduleKind == ScheduleKind.SELECTED_DAYS) {
                            append(" · ${scheduleDaysSummary(schedule.dayMask)}")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                color = statusColors.first,
                contentColor = statusColors.second,
                shape = RoundedCornerShape(AppRadii.Small),
            ) {
                Text(
                    text = statusLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

internal fun medicineDoseStatusLabel(
    status: IntakeStatus?,
    scheduledAt: Long?,
    nowMillis: Long,
): String = when {
    status == IntakeStatus.TAKEN -> "Принято"
    status == IntakeStatus.SKIPPED -> "Пропущено"
    status == null || scheduledAt == null -> "Не сегодня"
    scheduledAt > nowMillis -> "Предстоящее"
    else -> "Ожидает"
}

private data class DayPeriod(
    val title: String,
    val icon: ImageVector,
    val color: Color,
)

private fun medicineDayPeriod(minuteOfDay: Int): DayPeriod = when (minuteOfDay) {
    in 0 until 12 * 60 -> DayPeriod("Утро", Icons.Rounded.WbSunny, Color(0xFFE5A326))
    in 12 * 60 until 18 * 60 -> DayPeriod("День", Icons.Rounded.LightMode, Color(0xFFF0A43A))
    else -> DayPeriod("Вечер", Icons.Rounded.DarkMode, Color(0xFF6676AD))
}

private fun minuteOfDay(dose: ScheduledDose): Int {
    val time = Instant.ofEpochMilli(dose.scheduledAt)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return time.hour * 60 + time.minute
}

private fun formatMinuteOfDay(minuteOfDay: Int): String = String.format(
    Locale.ROOT,
    "%02d:%02d",
    minuteOfDay / 60,
    minuteOfDay % 60,
)

private fun scheduleDaysSummary(mask: Int): String {
    val titles = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    return titles.filterIndexed { index, _ -> mask and dayMask(index + 1) != 0 }.joinToString(", ")
}

private fun medicineCourseSummary(medicine: Medicine): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("ru"))
    val start = medicine.startDate.format(formatter)
    val end = medicine.endDate?.format(formatter)
    return if (end == null) "С $start, без даты окончания" else "$start — $end"
}

private fun MedicineState.detailTitle(): String = when (this) {
    MedicineState.ACTIVE -> "Активное лекарство"
    MedicineState.PAUSED -> "Приём приостановлен"
    MedicineState.ARCHIVED -> "Курс завершён"
}
