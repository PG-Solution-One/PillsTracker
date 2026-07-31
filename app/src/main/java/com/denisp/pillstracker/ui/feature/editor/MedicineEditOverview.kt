package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.DateWithYearFormatter
import com.denisp.pillstracker.ui.TimeFormatter
import com.denisp.pillstracker.ui.theme.AppRadii
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import java.time.LocalDate
import java.time.LocalTime

@Composable
internal fun MedicineEditOverview(
    draft: MedicineEditorDraft,
    onSelect: (MedicineEditSection) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs)) {
            Text(
                text = draft.name.ifBlank { "Лекарство" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Выберите, что хотите изменить",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        MedicineEditSection.entries.forEach { section ->
            EditSectionCard(
                section = section,
                summary = section.summary(draft),
                onClick = { onSelect(section) },
            )
        }
    }
}

@Composable
internal fun EditSectionHeader(section: MedicineEditSection) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs),
    ) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = section.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EditSectionCard(
    section: MedicineEditSection,
    summary: String,
    onClick: () -> Unit,
) {
    AppSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(AppRadii.Small),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Открыть раздел ${section.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val MedicineEditSection.icon: ImageVector
    get() = when (this) {
        MedicineEditSection.APPEARANCE -> Icons.Rounded.Edit
        MedicineEditSection.DOSAGE -> Icons.Rounded.Inventory2
        MedicineEditSection.COURSE -> Icons.Rounded.CalendarMonth
        MedicineEditSection.SCHEDULE -> Icons.Rounded.Schedule
        MedicineEditSection.INSTRUCTIONS -> Icons.Rounded.Restaurant
    }

private fun MedicineEditSection.summary(draft: MedicineEditorDraft): String = when (this) {
    MedicineEditSection.APPEARANCE ->
        "${draft.name.ifBlank { "Название не указано" }} · ${draft.form.title}"

    MedicineEditSection.DOSAGE -> buildString {
        val dosage = draft.dosage?.displayAmount() ?: draft.dosageAmount.ifBlank { "—" }
        append("$dosage ${draft.dosageUnit.title} · ${draft.tabletsPerIntake.ifBlank { "—" }} шт.")
        if (draft.trackStock) {
            append(" · осталось ${draft.remaining.ifBlank { "—" }}")
        }
    }

    MedicineEditSection.COURSE ->
        "${coursePeriodSummary(draft.startDate, draft.endDate)} · ${draft.scheduleKind.title}"

    MedicineEditSection.SCHEDULE -> if (draft.scheduleKind == ScheduleKind.AS_NEEDED) {
        draft.scheduleKind.title
    } else {
        draft.times
            .sortedBy(EditableScheduleTime::minuteOfDay)
            .joinToString(" · ") {
                LocalTime.of(it.minuteOfDay / 60, it.minuteOfDay % 60).format(TimeFormatter)
            }
    }

    MedicineEditSection.INSTRUCTIONS -> buildString {
        append(draft.mealTiming.title)
        if (draft.note.isNotBlank()) append(" · ${draft.note}")
    }
}

private fun coursePeriodSummary(startDate: LocalDate, endDate: LocalDate?): String =
    if (endDate == null) {
        "с ${startDate.format(DateWithYearFormatter)}, без окончания"
    } else {
        "${startDate.format(DateWithYearFormatter)} — ${endDate.format(DateWithYearFormatter)}"
    }
