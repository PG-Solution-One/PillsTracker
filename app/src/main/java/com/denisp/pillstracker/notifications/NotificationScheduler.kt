package com.denisp.pillstracker.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.denisp.pillstracker.MainActivity
import com.denisp.pillstracker.R
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.domain.ScheduleCalculator
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.notifications.receiver.AlarmReceiver
import com.denisp.pillstracker.notifications.receiver.NotificationActionReceiver
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale

class NotificationScheduler(
    private val context: Context,
    private val repository: TrackerRepository,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val _doseReminderEvents = MutableSharedFlow<DoseReminderEvent>(
        replay = 0,
        extraBufferCapacity = 16,
    )
    val doseReminderEvents = _doseReminderEvents.asSharedFlow()

    fun createChannels() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    MEDICINE_CHANNEL,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = context.getString(R.string.notification_channel_description)
                    enableVibration(true)
                },
                NotificationChannel(
                    STOCK_CHANNEL,
                    context.getString(R.string.stock_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            ),
        )
    }

    fun rescheduleAll() {
        val timestamps = ScheduleCalculator.upcomingTimestamps(
            medicines = repository.snapshot.value.medicines,
            fromMillis = System.currentTimeMillis(),
        )
        timestamps.forEach { scheduleInitial(it) }
    }

    fun cancelMedicineReminders(medicine: Medicine) {
        ScheduleCalculator.upcomingTimestamps(
            medicines = listOf(medicine),
            fromMillis = System.currentTimeMillis(),
        ).forEach { scheduledAt ->
            AlarmType.entries.forEach { type ->
                alarmManager.cancel(alarmPendingIntent(scheduledAt, type))
            }
            dismissDoseNotification(scheduledAt)
        }
        NotificationManagerCompat.from(context).cancel(
            STOCK_NOTIFICATION_BASE + medicine.id.toInt(),
        )
    }

    fun scheduleInitial(scheduledAt: Long) {
        scheduleAlarm(scheduledAt, scheduledAt, AlarmType.INITIAL)
    }

    fun scheduleSnoozed(scheduledAt: Long) {
        cancelFollowUps(scheduledAt)
        scheduleAlarm(
            triggerAt = System.currentTimeMillis() + SNOOZE_MILLIS,
            scheduledAt = scheduledAt,
            type = AlarmType.SNOOZED,
        )
    }

    fun scheduleInitialFollowUps(scheduledAt: Long) {
        val now = System.currentTimeMillis()
        val repeatAt = scheduledAt + SNOOZE_MILLIS
        val expireAt = scheduledAt + EXPIRE_MILLIS
        if (repeatAt > now) scheduleAlarm(repeatAt, scheduledAt, AlarmType.REPEAT)
        if (expireAt > now) {
            scheduleAlarm(expireAt, scheduledAt, AlarmType.EXPIRE)
        } else {
            markExpired(scheduledAt)
        }
    }

    fun scheduleSnoozedExpiry(scheduledAt: Long) {
        scheduleAlarm(
            triggerAt = System.currentTimeMillis() + EXPIRE_MILLIS,
            scheduledAt = scheduledAt,
            type = AlarmType.EXPIRE,
        )
    }

    fun cancelFollowUps(scheduledAt: Long) {
        listOf(AlarmType.REPEAT, AlarmType.SNOOZED, AlarmType.EXPIRE).forEach { type ->
            alarmManager.cancel(alarmPendingIntent(scheduledAt, type))
        }
    }

    @SuppressLint("MissingPermission")
    fun showDoseNotification(scheduledAt: Long) {
        val doses = repository.dosesAt(scheduledAt).filter { it.status == IntakeStatus.PENDING }
        if (doses.isEmpty()) return
        _doseReminderEvents.tryEmit(DoseReminderEvent(scheduledAt))
        if (!canPostNotifications()) return

        val names = doses.joinToString(", ") { it.medicine.name }
        val time = Instant.ofEpochMilli(scheduledAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru")))
        val title = context.getString(
            if (doses.size == 1) R.string.take_medicine else R.string.take_medicines,
        )
        val content = if (doses.size == 1) {
            "${doses.first().medicine.name} · ${doses.first().medicine.dosage}"
        } else {
            "$time · $names"
        }
        val openIntent = PendingIntent.getActivity(
            context,
            notificationId(scheduledAt),
            Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, MEDICINE_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(openIntent)
            .setAutoCancel(false)
            .setOngoing(false)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                0,
                context.getString(R.string.notification_take_all),
                actionPendingIntent(scheduledAt, NotificationAction.TAKE_ALL),
            )
            .addAction(
                0,
                context.getString(R.string.notification_skip_all),
                actionPendingIntent(scheduledAt, NotificationAction.SKIP_ALL),
            )
            .addAction(
                0,
                context.getString(R.string.notification_snooze),
                actionPendingIntent(scheduledAt, NotificationAction.SNOOZE),
            )
            .build()

        NotificationManagerCompat.from(context).notify(notificationId(scheduledAt), notification)
    }

    fun dismissDoseNotification(scheduledAt: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId(scheduledAt))
    }

    @SuppressLint("MissingPermission")
    fun showLowStockNotifications(medicines: List<Medicine>) {
        if (!canPostNotifications()) return
        medicines
            .filter { it.remaining <= it.tabletsPerIntake * 3 && it.remaining >= 0 }
            .forEach { medicine ->
                val text = "Осталось ${medicine.remaining.displayAmount()} шт. — не больше трёх приёмов"
                val notification = NotificationCompat.Builder(context, STOCK_CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Пора купить ${medicine.name}")
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            medicine.id.hashCode(),
                            Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        ),
                    )
                    .build()
                NotificationManagerCompat.from(context).notify(STOCK_NOTIFICATION_BASE + medicine.id.toInt(), notification)
            }
    }

    fun markExpired(scheduledAt: Long) {
        repository.markAll(scheduledAt, IntakeStatus.SKIPPED)
        dismissDoseNotification(scheduledAt)
    }

    private fun scheduleAlarm(triggerAt: Long, scheduledAt: Long, type: AlarmType) {
        val pendingIntent = alarmPendingIntent(scheduledAt, type)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun alarmPendingIntent(scheduledAt: Long, type: AlarmType): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode(scheduledAt, type.ordinal),
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_ALARM
                putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
                putExtra(EXTRA_ALARM_TYPE, type.name)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun actionPendingIntent(
        scheduledAt: Long,
        actionType: NotificationAction,
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode(scheduledAt, actionType.ordinal + 100),
        Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_NOTIFICATION
            putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
            putExtra(EXTRA_NOTIFICATION_ACTION, actionType.name)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun notificationId(scheduledAt: Long): Int = scheduledAt.hashCode()

    private fun requestCode(scheduledAt: Long, salt: Int): Int = 31 * scheduledAt.hashCode() + salt

    enum class AlarmType {
        INITIAL,
        REPEAT,
        SNOOZED,
        EXPIRE,
    }

    enum class NotificationAction {
        TAKE_ALL,
        SKIP_ALL,
        SNOOZE,
    }

    companion object {
        const val EXTRA_SCHEDULED_AT = "scheduled_at"
        const val EXTRA_ALARM_TYPE = "alarm_type"
        const val EXTRA_NOTIFICATION_ACTION = "notification_action"
        private const val ACTION_ALARM = "com.denisp.pillstracker.ALARM"
        private const val ACTION_NOTIFICATION = "com.denisp.pillstracker.NOTIFICATION_ACTION"
        private const val MEDICINE_CHANNEL = "medicine_reminders"
        private const val STOCK_CHANNEL = "stock_reminders"
        private const val STOCK_NOTIFICATION_BASE = 500_000
        private const val SNOOZE_MILLIS = 10 * 60 * 1000L
        private const val EXPIRE_MILLIS = 3 * 60 * 60 * 1000L
    }
}
