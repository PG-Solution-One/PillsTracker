package com.denisp.pillstracker.ui.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.InterfaceMode
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.components.MedicineAppearance
import com.denisp.pillstracker.ui.components.SwipeableIntakeCard
import com.denisp.pillstracker.ui.theme.AppPrimaryButton
import com.denisp.pillstracker.ui.theme.AppSecondaryButton
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.LocalInterfaceMode

@Composable
internal fun TodayDoseCard(
    dose: ScheduledDose,
    isNext: Boolean,
    onStatus: (IntakeStatus) -> Unit,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    nowMillis: Long,
) {
    SwipeableIntakeCard(
        dose = dose,
        canEdit = true,
        onStatus = onStatus,
        modifier = Modifier.fillMaxWidth(),
        showScheduledTime = true,
        isNext = isNext,
        onClick = onOpen,
        onLongPress = onLongPress,
        medicineAppearanceSize = 64.dp,
        prominentScheduledTime = true,
        nowMillis = nowMillis,
    )
}

@Composable
internal fun LowStockMedicineCard(
    medicine: Medicine,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
) {
    val simplified = LocalInterfaceMode.current == InterfaceMode.SIMPLIFIED
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onOpen, onLongClick = onLongPress),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        if (simplified) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MedicineDot(medicine)
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = AppSpacing.Md),
                    ) {
                        Text("Пора купить ${medicine.name}", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Осталось ${medicine.remaining.displayAmount()} шт.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
                AppSecondaryButton(
                    onClick = onLongPress,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Действия с лекарством")
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MedicineDot(medicine)
                Column(Modifier.padding(start = 12.dp)) {
                    Text("Пора купить ${medicine.name}", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Осталось ${medicine.remaining.displayAmount()} шт.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AsNeededMedicineCard(
    medicine: Medicine,
    onTaken: () -> Unit,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
) {
    val simplified = LocalInterfaceMode.current == InterfaceMode.SIMPLIFIED
    Card(
        modifier = Modifier.combinedClickable(onClick = onOpen, onLongClick = onLongPress),
        shape = RoundedCornerShape(18.dp),
    ) {
        if (simplified) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MedicineDot(medicine)
                    MedicineSummary(medicine = medicine, modifier = Modifier.weight(1f))
                }
                AppPrimaryButton(
                    onClick = onTaken,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Принять сейчас")
                }
                AppSecondaryButton(
                    onClick = onLongPress,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Действия с лекарством")
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MedicineDot(medicine)
                MedicineSummary(medicine = medicine, modifier = Modifier.weight(1f))
                Button(onClick = onTaken) { Text("Принять") }
            }
        }
    }
}

@Composable
private fun MedicineSummary(
    medicine: Medicine,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.padding(horizontal = 12.dp),
    ) {
        Text(
            text = medicine.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "${medicine.dosage} · " +
                "${medicine.tabletsPerIntake.displayAmount()} шт.",
        )
        if (medicine.trackStock) {
            Text(
                "Осталось ${medicine.remaining.displayAmount()} шт.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun TodaySectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
private fun MedicineDot(medicine: Medicine) {
    MedicineAppearance(medicine = medicine, size = 52.dp)
}
