package com.denisp.pillstracker.notifications.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.denisp.pillstracker.PillsTrackerApplication
import com.denisp.pillstracker.notifications.ExactAlarmAccess

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action !in setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
                AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
            )
        ) {
            return
        }
        if (
            intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED &&
            !ExactAlarmAccess.isGranted(context)
        ) {
            return
        }
        (context.applicationContext as PillsTrackerApplication).notificationScheduler.rescheduleAll()
    }
}
