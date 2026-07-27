package com.denisp.pillstracker.ui.components

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDateTimePickerDialogsTest {
    @Test
    fun `UTC date conversion keeps calendar day`() {
        val dates = listOf(
            LocalDate.of(1970, 1, 1),
            LocalDate.of(2026, 7, 28),
            LocalDate.of(2030, 12, 31),
        )

        dates.forEach { date ->
            assertEquals(date, utcMillisToLocalDate(date.toUtcDateMillis()))
        }
    }

    @Test
    fun `date range includes boundaries and excludes outside dates`() {
        val start = LocalDate.of(2026, 7, 28)
        val end = LocalDate.of(2026, 8, 10)

        assertTrue(isDateInRange(start, start, end))
        assertTrue(isDateInRange(end, start, end))
        assertTrue(isDateInRange(LocalDate.of(2026, 8, 1), start, end))
        assertFalse(isDateInRange(start.minusDays(1), start, end))
        assertFalse(isDateInRange(end.plusDays(1), start, end))
    }

    @Test
    fun `hour and minute convert to minute of day`() {
        assertEquals(0, minuteOfDay(hour = 0, minute = 0))
        assertEquals(8 * 60 + 30, minuteOfDay(hour = 8, minute = 30))
        assertEquals(1439, minuteOfDay(hour = 23, minute = 59))
    }
}
