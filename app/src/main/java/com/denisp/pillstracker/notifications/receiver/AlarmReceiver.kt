package com.denisp.pillstracker.notifications.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.denisp.pillstracker.PillsTrackerApplication
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.notifications.NotificationScheduler.AlarmType
import com.denisp.pillstracker.notifications.NotificationScheduler.Companion.EXTRA_ALARM_TYPE
import com.denisp.pillstracker.notifications.NotificationScheduler.Companion.EXTRA_SCHEDULED_AT

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, -1L)
        val type = intent.getStringExtra(EXTRA_ALARM_TYPE)?.let(AlarmType::valueOf) ?: return
        if (scheduledAt < 0) return
        val scheduler = (context.applicationContext as PillsTrackerApplication).notificationScheduler
        when (type) {
            AlarmType.INITIAL -> {
                scheduler.showDoseNotification(scheduledAt)
                scheduler.scheduleInitialFollowUps(scheduledAt)
            }
            AlarmType.REPEAT -> scheduler.showDoseNotification(scheduledAt)
            AlarmType.SNOOZED -> {
                scheduler.showDoseNotification(scheduledAt)
                scheduler.scheduleSnoozedExpiry(scheduledAt)
            }
            AlarmType.EXPIRE -> scheduler.markExpired(scheduledAt)
        }
    }
}
