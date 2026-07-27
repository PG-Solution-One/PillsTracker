package com.denisp.pillstracker.ui.feature.today

import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.TrackerSnapshot
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayUiStateTest {
    @Test
    fun `state keeps only active medicines and selects next pending dose`() {
        val regular = medicine(id = 1, remaining = 2.0)
        val asNeeded = medicine(id = 2, scheduleKind = ScheduleKind.AS_NEEDED)
        val paused = medicine(id = 3, state = MedicineState.PAUSED)
        val doses = listOf(
            ScheduledDose(regular, scheduledAt = 100, status = IntakeStatus.TAKEN),
            ScheduledDose(regular, scheduledAt = 200, status = IntakeStatus.PENDING),
        )

        val state = buildTodayUiState(
            snapshot = TrackerSnapshot(listOf(regular, asNeeded, paused)),
            doses = doses,
            nowMillis = 150,
        )

        assertEquals(listOf(regular, asNeeded), state.activeMedicines)
        assertEquals(listOf(asNeeded), state.asNeededMedicines)
        assertEquals(listOf(regular), state.lowStockMedicines)
        assertEquals(200L, state.nextDose?.scheduledAt)
        assertEquals(1, state.takenToday)
        assertEquals(2, state.totalToday)
    }

    @Test
    fun `overdue pending dose is used when there is no future dose`() {
        val medicine = medicine(id = 1)
        val overdue = ScheduledDose(
            medicine = medicine,
            scheduledAt = 100,
            status = IntakeStatus.PENDING,
        )

        val state = buildTodayUiState(
            snapshot = TrackerSnapshot(listOf(medicine)),
            doses = listOf(overdue),
            nowMillis = 200,
        )

        assertEquals(overdue, state.nextDose)
    }

    private fun medicine(
        id: Long,
        remaining: Double = 30.0,
        state: MedicineState = MedicineState.ACTIVE,
        scheduleKind: ScheduleKind = ScheduleKind.DAILY,
    ) = Medicine(
        id = id,
        name = "Лекарство $id",
        form = MedicineForm.TABLET,
        colorArgb = 0xFF147D64,
        dosageAmount = 500.0,
        dosageUnit = DosageUnit.MG,
        tabletsPerIntake = 1.0,
        packageSize = 30.0,
        remaining = remaining,
        mealTiming = MealTiming.ANY,
        note = "",
        startDate = LocalDate.of(2026, 7, 20),
        endDate = null,
        scheduleKind = scheduleKind,
        state = state,
    )
}
