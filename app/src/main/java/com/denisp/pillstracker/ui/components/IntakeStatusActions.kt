package com.denisp.pillstracker.ui.components

import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.notifications.NotificationScheduler

fun updateIntakeStatus(
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
    dose: ScheduledDose,
    status: IntakeStatus,
) {
    repository.markIntake(dose.medicine.id, dose.scheduledAt, status)
    if (status == IntakeStatus.TAKEN) {
        scheduler.showLowStockNotifications(
            repository.snapshot.value.medicines.filter { it.id == dose.medicine.id },
        )
    }
    if (repository.dosesAt(dose.scheduledAt).none { it.status == IntakeStatus.PENDING }) {
        scheduler.cancelFollowUps(dose.scheduledAt)
        scheduler.dismissDoseNotification(dose.scheduledAt)
    }
}

fun updateIntakeGroupStatus(
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
    scheduledAt: Long,
    status: IntakeStatus,
) {
    val changedMedicines = repository.markAll(scheduledAt, status)
    if (status == IntakeStatus.TAKEN && changedMedicines.isNotEmpty()) {
        val changedIds = changedMedicines.map { it.id }.toSet()
        scheduler.showLowStockNotifications(
            repository.snapshot.value.medicines.filter { it.id in changedIds },
        )
    }
    if (repository.dosesAt(scheduledAt).none { it.status == IntakeStatus.PENDING }) {
        scheduler.cancelFollowUps(scheduledAt)
        scheduler.dismissDoseNotification(scheduledAt)
    }
}
