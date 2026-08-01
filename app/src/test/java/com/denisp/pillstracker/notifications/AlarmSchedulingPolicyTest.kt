package com.denisp.pillstracker.notifications

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmSchedulingPolicyTest {
    @Test
    fun `only user-timed reminders require exact delivery`() {
        assertTrue(AlarmPurpose.SCHEDULED_DOSE.requiresExactTiming)
        assertTrue(AlarmPurpose.USER_SNOOZE.requiresExactTiming)
        assertFalse(AlarmPurpose.AUTOMATIC_REPEAT.requiresExactTiming)
        assertFalse(AlarmPurpose.DAY_END.requiresExactTiming)
    }
}
