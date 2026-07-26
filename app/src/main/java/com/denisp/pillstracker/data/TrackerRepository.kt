package com.denisp.pillstracker.data

import com.denisp.pillstracker.domain.ScheduleCalculator
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.TrackerSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.ZoneId

class TrackerRepository(private val database: TrackerDatabase) {
    private val _snapshot = MutableStateFlow(TrackerSnapshot())
    val snapshot: StateFlow<TrackerSnapshot> = _snapshot.asStateFlow()

    init {
        refresh()
    }

    @Synchronized
    fun refresh() {
        val zone = ZoneId.systemDefault()
        val from = LocalDate.now(zone).minusDays(45).atStartOfDay(zone).toInstant().toEpochMilli()
        val to = LocalDate.now(zone).plusDays(45).atStartOfDay(zone).toInstant().toEpochMilli()
        _snapshot.value = TrackerSnapshot(
            medicines = database.loadMedicines(),
            records = database.loadRecords(from, to),
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

    fun refill(medicineId: Long, amount: Double) {
        database.updateRemaining(medicineId, amount)
        refresh()
    }

    fun markIntake(medicineId: Long, scheduledAt: Long, status: IntakeStatus) {
        database.markIntake(medicineId, scheduledAt, status)
        refresh()
    }

    fun markAll(scheduledAt: Long, status: IntakeStatus): List<Medicine> {
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

    fun dosesAt(scheduledAt: Long): List<ScheduledDose> {
        val date = java.time.Instant.ofEpochMilli(scheduledAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return dosesForDate(date).filter { it.scheduledAt == scheduledAt }
    }
}
