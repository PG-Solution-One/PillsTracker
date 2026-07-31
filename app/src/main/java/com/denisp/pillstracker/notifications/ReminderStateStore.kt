package com.denisp.pillstracker.notifications

import android.content.Context
import androidx.core.content.edit

internal class ReminderStateStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    fun loadCycle(scheduledAt: Long): ReminderCycleState? {
        val cycleKey = cycleStartedAtKey(scheduledAt)
        if (!preferences.contains(cycleKey)) return null
        return ReminderCycleState(
            cycleStartedAt = preferences.getLong(cycleKey, scheduledAt),
            nextStage = preferences.getInt(nextStageKey(scheduledAt), 0),
        )
    }

    fun saveCycle(scheduledAt: Long, cycleStartedAt: Long, nextStage: Int) {
        preferences.edit {
            putLong(cycleStartedAtKey(scheduledAt), cycleStartedAt)
            putInt(nextStageKey(scheduledAt), nextStage)
        }
    }

    fun clearCycle(scheduledAt: Long) {
        preferences.edit {
            remove(cycleStartedAtKey(scheduledAt))
            remove(nextStageKey(scheduledAt))
        }
    }

    fun trackedTimestamps(): Set<Long> =
        preferences.getStringSet(KEY_TRACKED_TIMESTAMPS, emptySet())
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .toSet()

    fun track(scheduledAt: Long) {
        val updated = preferences.getStringSet(KEY_TRACKED_TIMESTAMPS, emptySet())
            .orEmpty()
            .toMutableSet()
            .apply { add(scheduledAt.toString()) }
        preferences.edit { putStringSet(KEY_TRACKED_TIMESTAMPS, updated) }
    }

    fun untrack(scheduledAt: Long) {
        val updated = preferences.getStringSet(KEY_TRACKED_TIMESTAMPS, emptySet())
            .orEmpty()
            .toMutableSet()
            .apply { remove(scheduledAt.toString()) }
        preferences.edit { putStringSet(KEY_TRACKED_TIMESTAMPS, updated) }
    }

    fun clearTrackedTimestamps() {
        preferences.edit { remove(KEY_TRACKED_TIMESTAMPS) }
    }

    private fun cycleStartedAtKey(scheduledAt: Long) = "cycle_started_at_$scheduledAt"

    private fun nextStageKey(scheduledAt: Long) = "next_stage_$scheduledAt"

    private companion object {
        const val PREFERENCES_NAME = "reminder_state"
        const val KEY_TRACKED_TIMESTAMPS = "tracked_timestamps"
    }
}

internal data class ReminderCycleState(
    val cycleStartedAt: Long,
    val nextStage: Int,
)
