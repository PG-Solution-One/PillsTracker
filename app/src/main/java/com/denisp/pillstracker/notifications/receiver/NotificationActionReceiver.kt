package com.denisp.pillstracker.notifications.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.denisp.pillstracker.PillsTrackerApplication
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.notifications.NotificationScheduler.Companion.EXTRA_NOTIFICATION_ACTION
import com.denisp.pillstracker.notifications.NotificationScheduler.Companion.EXTRA_SCHEDULED_AT
import com.denisp.pillstracker.notifications.NotificationScheduler.NotificationAction
import com.denisp.pillstracker.ui.components.updateIntakeGroupStatus

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, -1L)
        val action = intent.getStringExtra(EXTRA_NOTIFICATION_ACTION)
            ?.let(NotificationAction::valueOf)
            ?: return
        if (scheduledAt < 0) return

        val application = context.applicationContext as PillsTrackerApplication
        val repository = application.repository
        val scheduler = application.notificationScheduler

        when (action) {
            NotificationAction.TAKE_ALL -> {
                updateIntakeGroupStatus(
                    repository = repository,
                    scheduler = scheduler,
                    scheduledAt = scheduledAt,
                    status = IntakeStatus.TAKEN,
                )
            }
            NotificationAction.SKIP_ALL -> {
                updateIntakeGroupStatus(
                    repository = repository,
                    scheduler = scheduler,
                    scheduledAt = scheduledAt,
                    status = IntakeStatus.SKIPPED,
                )
            }
            NotificationAction.SNOOZE -> {
                scheduler.dismissDoseNotification(scheduledAt)
                scheduler.scheduleSnoozed(scheduledAt)
            }
        }
    }
}
