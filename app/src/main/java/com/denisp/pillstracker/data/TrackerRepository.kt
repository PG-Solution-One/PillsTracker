package com.denisp.pillstracker.data

import com.denisp.pillstracker.data.local.TrackerDatabase
import com.denisp.pillstracker.domain.IntakeRules
import com.denisp.pillstracker.domain.ScheduleCalculator
import com.denisp.pillstracker.domain.StockRules
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.TrackerSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId

class TrackerRepository(private val database: TrackerDatabase) {
    private val _snapshot = MutableStateFlow(TrackerSnapshot())
    val snapshot: StateFlow<TrackerSnapshot> = _snapshot.asStateFlow()

    init {
        refresh()
    }

    @Synchronized
    fun refresh() {
        _snapshot.value = TrackerSnapshot(
            medicines = database.loadMedicines(),
            records = database.loadRecords(),
        )
    }

    fun saveMedicine(medicine: Medicine): Long {
        val id = database.saveMedicine(medicine)
        refresh()
        return id
    }

    fun setMedicineState(medicineId: Long, state: MedicineState) {
        database.updateMedicineState(medicineId, state)
        refresh()
    }

    fun deleteMedicine(medicineId: Long): Boolean {
        val deleted = database.deleteMedicine(medicineId)
        if (deleted) refresh()
        return deleted
    }

    fun refill(medicineId: Long, addedAmount: Double) {
        val medicine = _snapshot.value.medicines.firstOrNull { it.id == medicineId } ?: return
        if (!medicine.trackStock) return
        if (
            StockRules.remainingAfterRefill(
                currentRemaining = medicine.remaining,
                addedAmount = addedAmount,
            ) == null
        ) {
            return
        }
        database.addRemaining(medicineId, addedAmount)
        refresh()
    }

    fun markIntake(medicineId: Long, scheduledAt: Long, status: IntakeStatus) {
        if (!IntakeRules.canChangeStatus(scheduledAt)) return
        if (_snapshot.value.medicines.none { it.id == medicineId }) return
        database.markIntake(medicineId, scheduledAt, status)
        refresh()
    }

    fun markAll(scheduledAt: Long, status: IntakeStatus): List<Medicine> {
        if (!IntakeRules.canChangeStatus(scheduledAt)) return emptyList()
        val medicines = dosesAt(scheduledAt)
            .filter { it.status == IntakeStatus.PENDING }
            .map { it.medicine }
        medicines.forEach { database.markIntake(it.id, scheduledAt, status) }
        refresh()
        return medicines
    }

    fun dosesForDate(date: LocalDate, activeOnly: Boolean = true): List<ScheduledDose> =
        ScheduleCalculator.dosesForDate(
            medicines = _snapshot.value.medicines,
            records = _snapshot.value.records,
            date = date,
            activeOnly = activeOnly,
        )

    fun dosesForDateIncludingManual(
        date: LocalDate,
        activeOnly: Boolean = false,
    ): List<ScheduledDose> {
        val scheduledDoses = dosesForDate(date, activeOnly)
        val scheduledKeys = scheduledDoses.map { it.medicine.id to it.scheduledAt }.toSet()
        val medicinesById = _snapshot.value.medicines.associateBy { it.id }
        val manualDoses = _snapshot.value.records
            .asSequence()
            .filter { record ->
                val recordDate = Instant.ofEpochMilli(record.scheduledAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                recordDate == date && (record.medicineId to record.scheduledAt) !in scheduledKeys
            }
            .mapNotNull { record ->
                medicinesById[record.medicineId]?.let { medicine ->
                    ScheduledDose(
                        medicine = medicine,
                        scheduledAt = record.scheduledAt,
                        status = record.status,
                        updatedAt = record.updatedAt,
                    )
                }
            }
            .toList()
        return (scheduledDoses + manualDoses).sortedBy { it.scheduledAt }
    }

    fun dosesAt(scheduledAt: Long): List<ScheduledDose> {
        val date = java.time.Instant.ofEpochMilli(scheduledAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return dosesForDate(date).filter { it.scheduledAt == scheduledAt }
    }
}
