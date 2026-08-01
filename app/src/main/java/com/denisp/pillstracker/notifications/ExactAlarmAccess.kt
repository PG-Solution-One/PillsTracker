package com.denisp.pillstracker.notifications

import android.app.AlarmManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri

internal object ExactAlarmAccess {
    val isRequired: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isGranted(context: Context): Boolean =
        !isRequired || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    @SuppressLint("InlinedApi")
    fun settingsIntent(context: Context): Intent {
        check(isRequired) { "Exact alarm settings are only available on Android 12+" }
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = "package:${context.packageName}".toUri()
        }
    }
}
