package com.denisp.pillstracker.ui.feature.today

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GreetingPeriodTest {
    @Test
    fun `periods change at expected hour boundaries`() {
        assertEquals(GreetingPeriod.NIGHT, greetingPeriodForHour(4))
        assertEquals(GreetingPeriod.MORNING, greetingPeriodForHour(5))
        assertEquals(GreetingPeriod.MORNING, greetingPeriodForHour(11))
        assertEquals(GreetingPeriod.DAY, greetingPeriodForHour(12))
        assertEquals(GreetingPeriod.DAY, greetingPeriodForHour(17))
        assertEquals(GreetingPeriod.EVENING, greetingPeriodForHour(18))
        assertEquals(GreetingPeriod.EVENING, greetingPeriodForHour(22))
        assertEquals(GreetingPeriod.NIGHT, greetingPeriodForHour(23))
    }

    @Test
    fun `invalid hours are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            greetingPeriodForHour(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            greetingPeriodForHour(24)
        }
    }
}
