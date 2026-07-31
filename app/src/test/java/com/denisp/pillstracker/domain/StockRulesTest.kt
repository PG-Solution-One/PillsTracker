package com.denisp.pillstracker.domain

import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.ScheduleKind
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StockRulesTest {
    @Test
    fun `refill adds a new package to current remaining stock`() {
        assertEquals(
            30.0,
            StockRules.remainingAfterRefill(
                currentRemaining = 5.0,
                addedAmount = 25.0,
            ) ?: error("Valid refill must produce a remaining amount"),
            0.0,
        )
    }

    @Test
    fun `refill rejects non-positive and non-finite amounts`() {
        assertNull(StockRules.remainingAfterRefill(currentRemaining = 5.0, addedAmount = 0.0))
        assertNull(StockRules.remainingAfterRefill(currentRemaining = 5.0, addedAmount = -1.0))
        assertNull(StockRules.remainingAfterRefill(currentRemaining = 5.0, addedAmount = Double.NaN))
    }

    @Test
    fun `low stock means no more than three valid tracked intakes`() {
        assertTrue(StockRules.isLowStock(medicine(remaining = 3.0)))
        assertTrue(StockRules.isLowStock(medicine(remaining = 0.0)))
        assertFalse(StockRules.isLowStock(medicine(remaining = 3.1)))
        assertFalse(StockRules.isLowStock(medicine(remaining = -1.0)))
        assertFalse(StockRules.isLowStock(medicine(remaining = 0.0, trackStock = false)))
        assertFalse(StockRules.isLowStock(medicine(remaining = 0.0, tabletsPerIntake = 0.0)))
    }

    private fun medicine(
        remaining: Double,
        trackStock: Boolean = true,
        tabletsPerIntake: Double = 1.0,
    ) = Medicine(
        name = "Тест",
        form = MedicineForm.TABLET,
        colorArgb = 0xFF147D64,
        dosageAmount = 500.0,
        dosageUnit = DosageUnit.MG,
        tabletsPerIntake = tabletsPerIntake,
        packageSize = 30.0,
        remaining = remaining,
        trackStock = trackStock,
        mealTiming = MealTiming.ANY,
        note = "",
        startDate = LocalDate.of(2026, 7, 20),
        endDate = null,
        scheduleKind = ScheduleKind.DAILY,
    )
}
