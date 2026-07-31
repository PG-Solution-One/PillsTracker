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
        assertEquals(listOf(100L, 200L), state.doseGroups.map { it.scheduledAt })
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

    @Test
    fun `overdue dose has priority over a future dose`() {
        val medicine = medicine(id = 1)
        val overdue = ScheduledDose(medicine, 1_000, IntakeStatus.PENDING)
        val future = ScheduledDose(medicine, 4_000_000, IntakeStatus.PENDING)

        val state = buildTodayUiState(
            snapshot = TrackerSnapshot(listOf(medicine)),
            doses = listOf(overdue, future),
            nowMillis = 2_000_000,
        )

        assertEquals(overdue, state.nextDose)
    }

    @Test
    fun `medicine without stock tracking is excluded from low stock list`() {
        val untracked = medicine(id = 1, remaining = 0.0, trackStock = false)

        val state = buildTodayUiState(
            snapshot = TrackerSnapshot(listOf(untracked)),
            doses = emptyList(),
            nowMillis = 100,
        )

        assertEquals(emptyList<Medicine>(), state.lowStockMedicines)
    }

    @Test
    fun `doses with the same time are grouped without changing dose order`() {
        val firstMedicine = medicine(id = 1)
        val secondMedicine = medicine(id = 2)
        val laterMedicine = medicine(id = 3)
        val firstDose = ScheduledDose(
            medicine = firstMedicine,
            scheduledAt = 100,
            status = IntakeStatus.PENDING,
        )
        val secondDose = ScheduledDose(
            medicine = secondMedicine,
            scheduledAt = 100,
            status = IntakeStatus.TAKEN,
        )
        val laterDose = ScheduledDose(
            medicine = laterMedicine,
            scheduledAt = 200,
            status = IntakeStatus.PENDING,
        )

        val groups = groupTodayDoses(listOf(laterDose, firstDose, secondDose))

        assertEquals(listOf(100L, 200L), groups.map { it.scheduledAt })
        assertEquals(listOf(firstDose, secondDose), groups.first().doses)
        assertEquals(listOf(laterDose), groups.last().doses)
    }

    private fun medicine(
        id: Long,
        remaining: Double = 30.0,
        trackStock: Boolean = true,
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
        trackStock = trackStock,
        mealTiming = MealTiming.ANY,
        note = "",
        startDate = LocalDate.of(2026, 7, 20),
        endDate = null,
        scheduleKind = scheduleKind,
        state = state,
    )
}
