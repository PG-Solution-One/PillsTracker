package com.denisp.pillstracker.ui.components

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    @Test
    fun `date input uses russian mask`() {
        assertEquals("2", formatDateInput("2"))
        assertEquals("28.07", formatDateInput("2807"))
        assertEquals("28.07.2026", formatDateInput("28-07-2026"))
        assertEquals("05.11.1998", formatDateInput("28.07.202605111998"))
    }

    @Test
    fun `date input parses only valid complete dates`() {
        assertEquals(LocalDate.of(2026, 7, 28), parseDateInput("28.07.2026"))
        assertNull(parseDateInput("28.07"))
        assertNull(parseDateInput("31.02.2026"))
    }

    @Test
    fun `new time digits replace an existing value`() {
        assertEquals("21", normalizeTimeInput("0821"))
        assertEquals("45", normalizeTimeInput("3045"))
        assertEquals("21", replacementInput(previous = "08", changed = "0821"))
        assertEquals("2", replacementInput(previous = "08", changed = "082"))
    }

    @Test
    fun `time input accepts only existing hours and minutes`() {
        assertEquals("00", validatedTimeInput("00", maxExclusive = 24))
        assertEquals("23", validatedTimeInput("23", maxExclusive = 24))
        assertNull(validatedTimeInput("24", maxExclusive = 24))
        assertEquals("59", validatedTimeInput("59", maxExclusive = 60))
        assertNull(validatedTimeInput("60", maxExclusive = 60))
        assertNull(validatedTimeInput("99", maxExclusive = 60))
    }
}
