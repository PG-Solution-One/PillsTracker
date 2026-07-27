package com.denisp.pillstracker.ui.feature.today

import androidx.compose.foundation.layout.Column
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
import com.denisp.pillstracker.domain.IntakeRules
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.components.MedicineAppearance
import com.denisp.pillstracker.ui.components.SwipeableIntakeCard

@Composable
internal fun TodayDoseCard(
    dose: ScheduledDose,
    isNext: Boolean,
    onStatus: (IntakeStatus) -> Unit,
    onOpen: () -> Unit,
) {
    SwipeableIntakeCard(
        dose = dose,
        canEdit = true,
        onStatus = onStatus,
        modifier = Modifier.fillMaxWidth(),
        showScheduledTime = true,
        isNext = isNext,
        onClick = onOpen,
    )
}

@Composable
internal fun LowStockMedicineCard(
    medicine: Medicine,
    onOpen: () -> Unit,
) {
    Surface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
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

@Composable
internal fun AsNeededMedicineCard(
    medicine: Medicine,
    onTaken: () -> Unit,
    onOpen: () -> Unit,
) {
    val canTake = IntakeRules.canMarkTaken(
        remaining = medicine.remaining,
        tabletsPerIntake = medicine.tabletsPerIntake,
        currentStatus = IntakeStatus.PENDING,
    )
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MedicineDot(medicine)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(medicine.name, fontWeight = FontWeight.SemiBold)
                Text("${medicine.dosage} · осталось ${medicine.remaining.displayAmount()} шт.")
                if (!canTake) {
                    Text(
                        "Лекарство закончилось",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Button(onClick = onTaken, enabled = canTake) { Text("Принял") }
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
    MedicineAppearance(medicine = medicine, size = 25.dp)
}
