package com.denisp.pillstracker.ui

import com.denisp.pillstracker.ui.components.calculateAge
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class AgePickerFieldTest {
    @Test
    fun `age changes only after birthday`() {
        val birthDate = LocalDate.of(1997, 8, 10)

        assertEquals(28, calculateAge(birthDate, LocalDate.of(2026, 8, 9)))
        assertEquals(29, calculateAge(birthDate, LocalDate.of(2026, 8, 10)))
    }

    @Test
    fun `infant age is zero`() {
        assertEquals(
            0,
            calculateAge(
                birthDate = LocalDate.of(2026, 1, 1),
                today = LocalDate.of(2026, 7, 27),
            ),
        )
    }
}
