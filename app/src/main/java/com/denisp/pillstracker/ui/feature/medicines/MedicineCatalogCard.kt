package com.denisp.pillstracker.ui.feature.medicines

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.asTime
import com.denisp.pillstracker.ui.components.MedicineAppearance
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import java.time.LocalDate
import java.time.ZoneId

@Composable
internal fun MedicineCatalogCard(
    medicine: Medicine,
    onOpen: () -> Unit,
) {
    AppSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onOpen,
                onLongClick = onOpen,
            ),
        elevated = true,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MedicineAppearance(medicine = medicine, size = 44.dp)
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${medicine.form.title} · ${medicine.dosage}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(medicineScheduleSummary(medicine), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Осталось ${medicine.remaining.displayAmount()} из " +
                    "${medicine.packageSize.displayAmount()} шт.",
                color = if (medicine.remaining <= medicine.tabletsPerIntake * 3) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = "Нажмите или удерживайте для действий",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

internal fun medicineScheduleSummary(medicine: Medicine): String {
    if (medicine.scheduleKind == ScheduleKind.AS_NEEDED) return "По необходимости"
    val times = medicine.times.joinToString(", ") { schedule ->
        LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .plusMinutes(schedule.minuteOfDay.toLong())
            .toInstant()
            .toEpochMilli()
            .asTime()
    }
    return "${medicine.scheduleKind.title} · $times"
}
