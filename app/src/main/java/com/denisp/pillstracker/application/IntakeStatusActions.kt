package com.denisp.pillstracker.application

import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ScheduledDose

internal fun updateIntakeStatus(
    repository: TrackerRepository,
    notificationGateway: IntakeNotificationGateway,
    dose: ScheduledDose,
    status: IntakeStatus,
) {
    repository.markIntake(dose.medicine.id, dose.scheduledAt, status)
    if (status == IntakeStatus.TAKEN) {
        notificationGateway.showLowStockNotifications(
            repository.snapshot.value.medicines.filter { it.id == dose.medicine.id },
        )
    }
    if (repository.dosesAt(dose.scheduledAt).none { it.status == IntakeStatus.PENDING }) {
        notificationGateway.cancelFollowUps(dose.scheduledAt)
        notificationGateway.dismissDoseNotification(dose.scheduledAt)
    }
}

internal fun updateIntakeGroupStatus(
    repository: TrackerRepository,
    notificationGateway: IntakeNotificationGateway,
    scheduledAt: Long,
    status: IntakeStatus,
) {
    val changedMedicines = repository.markAll(scheduledAt, status)
    if (status == IntakeStatus.TAKEN && changedMedicines.isNotEmpty()) {
        val changedIds = changedMedicines.map { it.id }.toSet()
        notificationGateway.showLowStockNotifications(
            repository.snapshot.value.medicines.filter { it.id in changedIds },
        )
    }
    if (repository.dosesAt(scheduledAt).none { it.status == IntakeStatus.PENDING }) {
        notificationGateway.cancelFollowUps(scheduledAt)
        notificationGateway.dismissDoseNotification(scheduledAt)
    }
}
