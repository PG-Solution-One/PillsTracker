package com.denisp.pillstracker.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderStateStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val store = ReminderStateStore(context)
    private val scheduledAt = 1_784_915_400_000L

    @After
    fun tearDown() {
        store.clearCycle(scheduledAt)
    }

    @Test
    fun exactRepeatFlagSurvivesStateRestore() {
        store.saveCycle(
            scheduledAt = scheduledAt,
            cycleStartedAt = scheduledAt + 1_000L,
            nextStage = 2,
            nextAlarmExact = true,
        )

        assertTrue(store.loadCycle(scheduledAt)?.nextAlarmExact == true)
    }

    @Test
    fun legacyCycleDefaultsToInexactRepeat() {
        context.getSharedPreferences("reminder_state", Context.MODE_PRIVATE)
            .edit()
            .putLong("cycle_started_at_$scheduledAt", scheduledAt)
            .putInt("next_stage_$scheduledAt", 1)
            .remove("next_alarm_exact_$scheduledAt")
            .commit()

        assertFalse(store.loadCycle(scheduledAt)?.nextAlarmExact ?: true)
    }
}
