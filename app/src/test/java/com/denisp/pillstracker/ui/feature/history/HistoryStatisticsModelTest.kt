package com.denisp.pillstracker.ui.feature.history

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryStatisticsModelTest {
    @Test
    fun `summary calculates adherence change totals and current streak`() {
        val today = LocalDate.of(2026, 7, 28)
        val days = listOf(
            DayStatistics(today.minusDays(2), taken = 0, skipped = 1, pending = 0),
            DayStatistics(today.minusDays(1), taken = 1, skipped = 0, pending = 1),
            DayStatistics(today, taken = 2, skipped = 0, pending = 0),
        )
        val previousDays = listOf(
            DayStatistics(today.minusDays(30), taken = 1, skipped = 1, pending = 0),
        )

        val summary = summarizeHistory(days, previousDays, today)

        assertEquals(0.75f, summary.adherence)
        assertEquals(3, summary.taken)
        assertEquals(1, summary.skipped)
        assertEquals(1, summary.pending)
        assertEquals(25, summary.adherenceChange)
        assertEquals(2, summary.streak)
    }

    @Test
    fun `empty history has no adherence or comparison`() {
        val summary = summarizeHistory(
            days = emptyList(),
            previousDays = emptyList(),
            today = LocalDate.of(2026, 7, 28),
        )

        assertNull(summary.adherence)
        assertNull(summary.adherenceChange)
        assertEquals(0, summary.streak)
    }
}
