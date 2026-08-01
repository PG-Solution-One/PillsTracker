package com.denisp.pillstracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.denisp.pillstracker.data.local.ProfilePreferences
import com.denisp.pillstracker.data.local.ThemePreferences
import com.denisp.pillstracker.model.InterfaceMode
import com.denisp.pillstracker.model.UserProfile
import com.denisp.pillstracker.notifications.ExactAlarmAccess
import com.denisp.pillstracker.notifications.NotificationScheduler.Companion.EXTRA_SCHEDULED_AT
import com.denisp.pillstracker.ui.PillsTrackerApp
import com.denisp.pillstracker.ui.theme.PillsTrackerTheme

class MainActivity : ComponentActivity() {
    private var notificationScheduledAt by mutableLongStateOf(-1L)
    private lateinit var themePreferences: ThemePreferences
    private lateinit var profilePreferences: ProfilePreferences
    private var themeMode by mutableStateOf(com.denisp.pillstracker.model.ThemeMode.SYSTEM)
    private var interfaceMode by mutableStateOf(InterfaceMode.STANDARD)
    private var userProfile by mutableStateOf(UserProfile())
    private var exactAlarmNoticeSeen by mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        themePreferences = ThemePreferences(this)
        profilePreferences = ProfilePreferences(this)
        themeMode = themePreferences.load()
        interfaceMode = themePreferences.loadInterfaceMode()
        userProfile = profilePreferences.load()
        exactAlarmNoticeSeen = profilePreferences.hasSeenExactAlarmNotice()
        notificationScheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, -1L)
        requestNotificationPermission()

        val application = application as PillsTrackerApplication
        if (
            !userProfile.onboardingCompleted &&
            application.repository.snapshot.value.medicines.isNotEmpty()
        ) {
            userProfile = userProfile.copy(onboardingCompleted = true)
            profilePreferences.save(userProfile)
        }
        if (
            userProfile.onboardingCompleted &&
            !exactAlarmNoticeSeen &&
            ExactAlarmAccess.isGranted(this)
        ) {
            markExactAlarmNoticeSeen()
        }
        setContent {
            PillsTrackerTheme(
                themeMode = themeMode,
                interfaceMode = interfaceMode,
            ) {
                PillsTrackerApp(
                    repository = application.repository,
                    scheduler = application.notificationScheduler,
                    openedScheduledAt = notificationScheduledAt.takeIf { it >= 0 },
                    onNotificationHandled = { notificationScheduledAt = -1L },
                    themeMode = themeMode,
                    interfaceMode = interfaceMode,
                    userProfile = userProfile,
                    showExactAlarmNotice = !exactAlarmNoticeSeen,
                    onThemeModeChanged = {
                        themeMode = it
                        themePreferences.save(it)
                    },
                    onInterfaceModeChanged = {
                        interfaceMode = it
                        themePreferences.saveInterfaceMode(it)
                    },
                    onUserProfileChanged = {
                        userProfile = it
                        profilePreferences.save(it)
                    },
                    onExactAlarmNoticeSeen = ::markExactAlarmNoticeSeen,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationScheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, -1L)
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun markExactAlarmNoticeSeen() {
        exactAlarmNoticeSeen = true
        profilePreferences.markExactAlarmNoticeSeen()
    }
}
