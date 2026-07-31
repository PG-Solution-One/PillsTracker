package com.denisp.pillstracker.notifications.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.denisp.pillstracker.PillsTrackerApplication
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.notifications.NotificationScheduler.AlarmType
import com.denisp.pillstracker.notifications.NotificationScheduler.Companion.EXTRA_ALARM_TYPE
import com.denisp.pillstracker.notifications.NotificationScheduler.Companion.EXTRA_CYCLE_STARTED_AT
import com.denisp.pillstracker.notifications.NotificationScheduler.Companion.EXTRA_REPEAT_STAGE
import com.denisp.pillstracker.notifications.NotificationScheduler.Companion.EXTRA_SCHEDULED_AT

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, -1L)
        val type = intent.getStringExtra(EXTRA_ALARM_TYPE)
            ?.let { runCatching { AlarmType.valueOf(it) }.getOrNull() }
            ?: return
        if (scheduledAt < 0) return
        val scheduler = (context.applicationContext as PillsTrackerApplication).notificationScheduler
        when (type) {
            AlarmType.INITIAL -> scheduler.handleInitial(scheduledAt)
            AlarmType.REPEAT -> scheduler.handleRepeat(
                scheduledAt = scheduledAt,
                cycleStartedAt = intent.getLongExtra(EXTRA_CYCLE_STARTED_AT, scheduledAt),
                stage = intent.getIntExtra(EXTRA_REPEAT_STAGE, 0),
            )
            AlarmType.SNOOZED -> scheduler.handleLegacySnoozed(scheduledAt)
            AlarmType.EXPIRE -> scheduler.handleLegacyExpiry(scheduledAt)
            AlarmType.DAY_END -> scheduler.markExpired(scheduledAt)
        }
    }
}
