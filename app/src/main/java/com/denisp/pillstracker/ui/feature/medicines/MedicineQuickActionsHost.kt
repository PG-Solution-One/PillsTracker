package com.denisp.pillstracker.ui.feature.medicines

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.notifications.NotificationScheduler

@Composable
internal fun MedicineQuickActionsHost(
    selectedMedicine: Medicine?,
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
    onDismiss: () -> Unit,
    onEdit: (Medicine) -> Unit,
    onChanged: () -> Unit,
) {
    var refillMedicine by remember { mutableStateOf<Medicine?>(null) }
    var deleteMedicine by remember { mutableStateOf<Medicine?>(null) }

    selectedMedicine?.let { medicine ->
        MedicineActionsSheet(
            medicine = medicine,
            onDismiss = onDismiss,
            onEdit = {
                onDismiss()
                onEdit(medicine)
            },
            onRefill = {
                onDismiss()
                refillMedicine = medicine
            },
            onStateChange = { state ->
                repository.setMedicineState(medicine.id, state)
                onChanged()
                onDismiss()
            },
            onDelete = {
                onDismiss()
                deleteMedicine = medicine
            },
        )
    }

    refillMedicine?.let { medicine ->
        RefillMedicineDialog(
            medicine = medicine,
            onDismiss = { refillMedicine = null },
            onSave = {
                repository.refill(medicine.id, it)
                refillMedicine = null
            },
        )
    }

    deleteMedicine?.let { medicine ->
        DeleteMedicineDialog(
            medicine = medicine,
            onDismiss = { deleteMedicine = null },
            onConfirm = {
                scheduler.cancelMedicineReminders(medicine)
                if (repository.deleteMedicine(medicine.id)) {
                    scheduler.rescheduleAll()
                }
                deleteMedicine = null
            },
        )
    }
}
