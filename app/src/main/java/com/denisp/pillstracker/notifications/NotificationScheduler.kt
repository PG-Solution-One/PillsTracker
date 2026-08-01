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
import com.denisp.pillstracker.domain.DoseTimingPolicy
import com.denisp.pillstracker.domain.StockRules
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.notifications.receiver.AlarmReceiver
import com.denisp.pillstracker.notifications.receiver.NotificationActionReceiver
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NotificationScheduler(
    private val context: Context,
    private val repository: TrackerRepository,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val stateStore = ReminderStateStore(context)
    private val _reminderEvents = MutableSharedFlow<Long>(
        replay = 0,
        extraBufferCapacity = 16,
    )
    val reminderEvents = _reminderEvents.asSharedFlow()

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

    fun rescheduleAll(resetExisting: Boolean = true) {
        val now = System.currentTimeMillis()
        if (resetExisting) {
            cancelTrackedAlarms()
        }
        reconcileRecentPast(now)
        pruneObsoleteReminders()
        if (resetExisting) {
            restoreActiveReminders(now)
        }
        ScheduleCalculator.upcomingTimestamps(
            medicines = repository.snapshot.value.medicines,
            fromMillis = now,
        ).forEach(::scheduleInitial)
    }

    fun cancelMedicineReminders(medicine: Medicine) {
        ScheduleCalculator.upcomingTimestamps(
            medicines = listOf(medicine),
            fromMillis = System.currentTimeMillis(),
        ).forEach { scheduledAt ->
            cancelAllAlarms(scheduledAt)
            stateStore.clearCycle(scheduledAt)
            stateStore.untrack(scheduledAt)
            dismissDoseNotification(scheduledAt)
        }
        dismissStockNotification(medicine.id)
    }

    fun scheduleInitial(scheduledAt: Long) {
        scheduleAlarm(
            triggerAt = scheduledAt,
            scheduledAt = scheduledAt,
            type = AlarmType.INITIAL,
            purpose = AlarmPurpose.SCHEDULED_DOSE,
        )
    }

    fun handleInitial(scheduledAt: Long) {
        if (!hasPendingDoses(scheduledAt)) {
            cancelFollowUps(scheduledAt)
            return
        }
        deliverReminder(scheduledAt)
        scheduleDayEnd(scheduledAt)
        scheduleNextRepeat(
            scheduledAt = scheduledAt,
            cycleStartedAt = scheduledAt,
            fromStage = 0,
            now = System.currentTimeMillis(),
        )
    }

    fun handleRepeat(
        scheduledAt: Long,
        cycleStartedAt: Long,
        stage: Int,
    ) {
        val now = System.currentTimeMillis()
        if (now >= ReminderPolicy.dayEndAt(scheduledAt)) {
            markExpired(scheduledAt)
            return
        }
        if (!hasPendingDoses(scheduledAt)) {
            cancelFollowUps(scheduledAt)
            dismissDoseNotification(scheduledAt)
            return
        }
        deliverReminder(scheduledAt)
        scheduleNextRepeat(
            scheduledAt = scheduledAt,
            cycleStartedAt = cycleStartedAt,
            fromStage = stage + 1,
            now = now,
        )
    }

    fun handleLegacySnoozed(scheduledAt: Long) = handleLegacyRetry(scheduledAt)

    fun handleLegacyExpiry(scheduledAt: Long) = handleLegacyRetry(scheduledAt)

    private fun handleLegacyRetry(scheduledAt: Long) {
        if (!hasPendingDoses(scheduledAt)) {
            cancelFollowUps(scheduledAt)
            return
        }
        val now = System.currentTimeMillis()
        deliverReminder(scheduledAt)
        scheduleDayEnd(scheduledAt)
        scheduleNextRepeat(
            scheduledAt = scheduledAt,
            cycleStartedAt = now,
            fromStage = 0,
            now = now,
            purpose = AlarmPurpose.USER_SNOOZE,
        )
    }

    fun scheduleSnoozed(scheduledAt: Long) {
        cancelRetryAlarm(scheduledAt)
        val now = System.currentTimeMillis()
        scheduleDayEnd(scheduledAt)
        scheduleNextRepeat(
            scheduledAt = scheduledAt,
            cycleStartedAt = now,
            fromStage = 0,
            now = now,
        )
    }

    fun cancelFollowUps(scheduledAt: Long) {
        cancelAllAlarms(scheduledAt)
        stateStore.clearCycle(scheduledAt)
        stateStore.untrack(scheduledAt)
        stateStore.deactivateReminder(scheduledAt)
    }

    fun activeReminderTimestamps(now: Long = System.currentTimeMillis()): List<Long> =
        restorableReminderTimestamps()
            .asSequence()
            .filter { scheduledAt ->
                scheduledAt <= now &&
                    ReminderPolicy.dayEndAt(scheduledAt) > now &&
                    hasPendingDoses(scheduledAt)
            }
            .sorted()
            .toList()

    @SuppressLint("MissingPermission")
    fun showDoseNotification(scheduledAt: Long) {
        val doses = repository.dosesAt(scheduledAt).filter { it.status == IntakeStatus.PENDING }
        if (doses.isEmpty() || !canPostNotifications()) return

        val names = doses.joinToString(", ") { it.medicine.name }
        val time = Instant.ofEpochMilli(scheduledAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru")))
        val isOverdue = doses.any { DoseTimingPolicy.isOverdue(it, System.currentTimeMillis()) }
        val title = context.getString(
            when {
                isOverdue && doses.size == 1 -> R.string.overdue_medicine
                isOverdue -> R.string.overdue_medicines
                doses.size == 1 -> R.string.take_medicine
                else -> R.string.take_medicines
            },
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

        val notificationBuilder = NotificationCompat.Builder(context, MEDICINE_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(openIntent)
            .setAutoCancel(false)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (doses.size > 1) {
            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle("$title · $time")
                .setSummaryText(context.getString(R.string.notification_choose_hint))
            doses.forEach { dose ->
                inboxStyle.addLine("${dose.medicine.name} · ${dose.medicine.dosage}")
            }
            notificationBuilder
                .setStyle(inboxStyle)
                .addAction(
                    0,
                    context.getString(R.string.notification_choose),
                    openIntent,
                )
                .addAction(
                    0,
                    context.getString(R.string.notification_taken_all),
                    actionPendingIntent(scheduledAt, NotificationAction.TAKE_ALL),
                )
                .addAction(
                    0,
                    context.getString(R.string.notification_snooze),
                    actionPendingIntent(scheduledAt, NotificationAction.SNOOZE),
                )
        } else {
            notificationBuilder
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .addAction(
                    0,
                    context.getString(R.string.notification_taken_single),
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
        }

        NotificationManagerCompat.from(context).notify(
            notificationId(scheduledAt),
            notificationBuilder.build(),
        )
    }

    fun dismissDoseNotification(scheduledAt: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId(scheduledAt))
    }

    fun dismissStockNotification(medicineId: Long) {
        NotificationManagerCompat.from(context).cancel(
            STOCK_NOTIFICATION_BASE + medicineId.toInt(),
        )
    }

    @SuppressLint("MissingPermission")
    fun showLowStockNotifications(medicines: List<Medicine>) {
        if (!canPostNotifications()) return
        medicines
            .filter(StockRules::isLowStock)
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
                NotificationManagerCompat.from(context).notify(
                    STOCK_NOTIFICATION_BASE + medicine.id.toInt(),
                    notification,
                )
            }
    }

    fun markExpired(scheduledAt: Long) {
        repository.markAll(scheduledAt, IntakeStatus.SKIPPED)
        cancelFollowUps(scheduledAt)
        dismissDoseNotification(scheduledAt)
    }

    private fun deliverReminder(scheduledAt: Long) {
        stateStore.activateReminder(scheduledAt)
        showDoseNotification(scheduledAt)
        _reminderEvents.tryEmit(scheduledAt)
    }

    private fun scheduleNextRepeat(
        scheduledAt: Long,
        cycleStartedAt: Long,
        fromStage: Int,
        now: Long,
        purpose: AlarmPurpose = AlarmPurpose.AUTOMATIC_REPEAT,
    ) {
        val repeat = ReminderPolicy.nextRepeat(
            cycleStartedAt = cycleStartedAt,
            fromStage = fromStage,
            now = now,
            dayEndAt = ReminderPolicy.dayEndAt(scheduledAt),
        )
        if (repeat == null) {
            stateStore.clearCycle(scheduledAt)
            cancelRetryAlarm(scheduledAt)
            return
        }
        stateStore.saveCycle(
            scheduledAt = scheduledAt,
            cycleStartedAt = cycleStartedAt,
            nextStage = repeat.stage,
            nextAlarmExact = purpose.requiresExactTiming,
        )
        scheduleAlarm(
            triggerAt = repeat.triggerAt,
            scheduledAt = scheduledAt,
            type = AlarmType.REPEAT,
            cycleStartedAt = cycleStartedAt,
            repeatStage = repeat.stage,
            purpose = purpose,
        )
    }

    private fun scheduleDayEnd(scheduledAt: Long) {
        val dayEndAt = ReminderPolicy.dayEndAt(scheduledAt)
        if (dayEndAt <= System.currentTimeMillis()) {
            markExpired(scheduledAt)
            return
        }
        scheduleAlarm(
            triggerAt = dayEndAt,
            scheduledAt = scheduledAt,
            type = AlarmType.DAY_END,
            purpose = AlarmPurpose.DAY_END,
        )
    }

    private fun restoreActiveReminders(now: Long) {
        val today = LocalDate.now()
        repository.dosesForDate(today)
            .asSequence()
            .filter { it.status == IntakeStatus.PENDING && it.scheduledAt < now }
            .map { it.scheduledAt }
            .distinct()
            .forEach { scheduledAt ->
                scheduleDayEnd(scheduledAt)
                val cycle = stateStore.loadCycle(scheduledAt) ?: return@forEach
                scheduleNextRepeat(
                    scheduledAt = scheduledAt,
                    cycleStartedAt = cycle.cycleStartedAt,
                    fromStage = cycle.nextStage,
                    now = now,
                    purpose = if (cycle.nextAlarmExact) {
                        AlarmPurpose.USER_SNOOZE
                    } else {
                        AlarmPurpose.AUTOMATIC_REPEAT
                    },
                )
            }
    }

    private fun reconcileRecentPast(now: Long) {
        val today = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        for (daysAgo in 1L..RECONCILIATION_DAYS) {
            val date = today.minusDays(daysAgo)
            repository.dosesForDate(date, activeOnly = false)
                .asSequence()
                .filter { it.status == IntakeStatus.PENDING }
                .map { it.scheduledAt }
                .distinct()
                .forEach(::markExpired)
        }
    }

    private fun pruneObsoleteReminders() {
        restorableReminderTimestamps()
            .filterNot(::hasPendingDoses)
            .forEach { scheduledAt ->
                cancelFollowUps(scheduledAt)
                dismissDoseNotification(scheduledAt)
            }
    }

    private fun restorableReminderTimestamps(): Set<Long> =
        stateStore.activeReminderTimestamps() +
            stateStore.trackedTimestamps().filter(stateStore::isReminderActive)

    private fun cancelTrackedAlarms() {
        stateStore.trackedTimestamps().forEach(::cancelAllAlarms)
        stateStore.clearTrackedTimestamps()
    }

    private fun cancelAllAlarms(scheduledAt: Long) {
        AlarmType.entries.forEach { type ->
            alarmManager.cancel(alarmPendingIntent(scheduledAt, type))
        }
    }

    private fun cancelRetryAlarm(scheduledAt: Long) {
        listOf(AlarmType.REPEAT, AlarmType.SNOOZED).forEach { type ->
            alarmManager.cancel(alarmPendingIntent(scheduledAt, type))
        }
    }

    private fun hasPendingDoses(scheduledAt: Long): Boolean =
        repository.dosesAt(scheduledAt).any { it.status == IntakeStatus.PENDING }

    private fun scheduleAlarm(
        triggerAt: Long,
        scheduledAt: Long,
        type: AlarmType,
        purpose: AlarmPurpose,
        cycleStartedAt: Long = scheduledAt,
        repeatStage: Int = 0,
    ) {
        val pendingIntent = alarmPendingIntent(
            scheduledAt = scheduledAt,
            type = type,
            cycleStartedAt = cycleStartedAt,
            repeatStage = repeatStage,
        )
        stateStore.track(scheduledAt)
        if (purpose.requiresExactTiming && ExactAlarmAccess.isGranted(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun alarmPendingIntent(
        scheduledAt: Long,
        type: AlarmType,
        cycleStartedAt: Long = scheduledAt,
        repeatStage: Int = 0,
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode(scheduledAt, type.ordinal),
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_ALARM
                putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
                putExtra(EXTRA_ALARM_TYPE, type.name)
                putExtra(EXTRA_CYCLE_STARTED_AT, cycleStartedAt)
                putExtra(EXTRA_REPEAT_STAGE, repeatStage)
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

    private fun requestCode(scheduledAt: Long, salt: Int): Int =
        31 * scheduledAt.hashCode() + salt

    enum class AlarmType {
        INITIAL,
        REPEAT,
        // Kept in 1.3.0 so alarms created by 1.2.x are consumed without applying old behavior.
        SNOOZED,
        EXPIRE,
        DAY_END,
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
        const val EXTRA_CYCLE_STARTED_AT = "cycle_started_at"
        const val EXTRA_REPEAT_STAGE = "repeat_stage"
        private const val ACTION_ALARM = "com.denisp.pillstracker.ALARM"
        private const val ACTION_NOTIFICATION = "com.denisp.pillstracker.NOTIFICATION_ACTION"
        private const val MEDICINE_CHANNEL = "medicine_reminders"
        private const val STOCK_CHANNEL = "stock_reminders"
        private const val STOCK_NOTIFICATION_BASE = 500_000
        private const val RECONCILIATION_DAYS = 30L
    }
}
