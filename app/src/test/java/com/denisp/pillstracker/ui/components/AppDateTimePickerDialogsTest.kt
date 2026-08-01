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
    fun `time input uses 24 hour mask`() {
        assertEquals("2", formatTimeInput("2"))
        assertEquals("21", formatTimeInput("21"))
        assertEquals("21:4", formatTimeInput("214"))
        assertEquals("21:45", formatTimeInput("21-45"))
    }

    @Test
    fun `time input parses only valid complete time`() {
        assertEquals(0, parseTimeInput("00:00"))
        assertEquals(21 * 60 + 45, parseTimeInput("21:45"))
        assertEquals(23 * 60 + 59, parseTimeInput("2359"))
        assertNull(parseTimeInput("21:4"))
        assertNull(parseTimeInput("24:00"))
        assertNull(parseTimeInput("12:60"))
    }

    @Test
    fun `first edit can replace existing combined time`() {
        assertEquals(
            "2145",
            replacementInput(previous = "08:30", changed = "08:302145"),
        )
    }
}
