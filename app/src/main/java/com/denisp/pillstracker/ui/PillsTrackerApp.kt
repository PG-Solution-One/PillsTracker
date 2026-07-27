package com.denisp.pillstracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.domain.IntakeRules
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.model.UserProfile
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.ui.components.MedicineReminderOverlay
import com.denisp.pillstracker.ui.feature.editor.MedicineEditorScreen
import com.denisp.pillstracker.ui.feature.history.HistoryScreen
import com.denisp.pillstracker.ui.feature.medicines.MedicinesScreen
import com.denisp.pillstracker.ui.feature.onboarding.OnboardingScreen
import com.denisp.pillstracker.ui.feature.settings.SettingsScreen
import com.denisp.pillstracker.ui.feature.today.TodayScreen

private enum class MainSection(val title: String, val icon: ImageVector) {
    TODAY("Главная", Icons.Rounded.Home),
    MEDICINES("Лекарства", Icons.Rounded.Medication),
    HISTORY("История", Icons.Rounded.History),
    SETTINGS("Настройки", Icons.Rounded.Settings),
}

@Composable
fun PillsTrackerApp(
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
    openedScheduledAt: Long?,
    onNotificationHandled: () -> Unit,
    themeMode: ThemeMode,
    userProfile: UserProfile,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onUserProfileChanged: (UserProfile) -> Unit,
) {
    val snapshot by repository.snapshot.collectAsStateWithLifecycle()
    var section by remember { mutableStateOf(MainSection.TODAY) }
    val sectionHistory = remember { mutableStateListOf<MainSection>() }
    var editedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val reminderQueue = remember { mutableStateListOf<Long>() }
    val enqueueReminder: (Long) -> Unit = { scheduledAt ->
        if (scheduledAt >= 0 && scheduledAt !in reminderQueue) {
            reminderQueue.add(scheduledAt)
            reminderQueue.sort()
        }
    }
    val finishReminder: (Long) -> Unit = { scheduledAt ->
        reminderQueue.remove(scheduledAt)
        if (openedScheduledAt == scheduledAt) onNotificationHandled()
    }

    LaunchedEffect(scheduler, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            scheduler.doseReminderEvents.collect { event ->
                enqueueReminder(event.scheduledAt)
            }
        }
    }
    LaunchedEffect(openedScheduledAt) {
        openedScheduledAt?.let(enqueueReminder)
    }

    val reminderScheduledAt = reminderQueue.firstOrNull()
    val reminderDoses = reminderScheduledAt
        ?.let(repository::dosesAt)
        .orEmpty()
        .filter { it.status == IntakeStatus.PENDING }

    LaunchedEffect(
        reminderScheduledAt,
        reminderDoses.map { "${it.medicine.id}:${it.status}" },
    ) {
        if (reminderScheduledAt != null && reminderDoses.isEmpty()) {
            finishReminder(reminderScheduledAt)
        }
    }

    BackHandler(
        enabled = !editorOpen &&
            (userProfile.onboardingCompleted || snapshot.medicines.isNotEmpty()) &&
            sectionHistory.isNotEmpty(),
    ) {
        section = sectionHistory.removeAt(sectionHistory.lastIndex)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            !userProfile.onboardingCompleted && snapshot.medicines.isEmpty() -> {
                OnboardingScreen(
                    initialProfile = userProfile,
                    onComplete = onUserProfileChanged,
                )
            }

            editorOpen -> {
                MedicineEditorScreen(
                    initialMedicine = editedMedicine,
                    onBack = { editorOpen = false },
                    onSave = { medicine ->
                        repository.saveMedicine(medicine)
                        scheduler.rescheduleAll()
                        editorOpen = false
                    },
                )
            }

            else -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            tonalElevation = 4.dp,
                        ) {
                            MainSection.entries.forEach { item ->
                                NavigationBarItem(
                                    selected = section == item,
                                    onClick = {
                                        if (section != item) {
                                            sectionHistory.add(section)
                                            section = item
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    },
                                    label = { Text(item.title) },
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (
                            section == MainSection.TODAY ||
                            section == MainSection.MEDICINES
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    editedMedicine = null
                                    editorOpen = true
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    },
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        when (section) {
                            MainSection.TODAY -> TodayScreen(
                                snapshot = snapshot,
                                repository = repository,
                                scheduler = scheduler,
                                userName = userProfile.name,
                                onShowMedicines = {
                                    if (section != MainSection.MEDICINES) {
                                        sectionHistory.add(section)
                                        section = MainSection.MEDICINES
                                    }
                                },
                            )

                            MainSection.MEDICINES -> MedicinesScreen(
                                snapshot = snapshot,
                                repository = repository,
                                scheduler = scheduler,
                                onEdit = {
                                    editedMedicine = it
                                    editorOpen = true
                                },
                                onChanged = scheduler::rescheduleAll,
                            )

                            MainSection.HISTORY -> HistoryScreen(
                                snapshot = snapshot,
                                repository = repository,
                                scheduler = scheduler,
                            )

                            MainSection.SETTINGS -> SettingsScreen(
                                themeMode = themeMode,
                                userProfile = userProfile,
                                onThemeModeChanged = onThemeModeChanged,
                                onUserProfileChanged = onUserProfileChanged,
                            )
                        }
                    }
                }
            }
        }

        if (reminderScheduledAt != null && reminderDoses.isNotEmpty()) {
            MedicineReminderOverlay(
                doses = reminderDoses,
                takeEnabled = reminderDoses.all { dose ->
                    IntakeRules.canMarkTaken(
                        remaining = dose.medicine.remaining,
                        tabletsPerIntake = dose.medicine.tabletsPerIntake,
                        currentStatus = dose.status,
                    )
                },
                onTakeAll = {
                    repository.markAll(reminderScheduledAt, IntakeStatus.TAKEN)
                    scheduler.cancelFollowUps(reminderScheduledAt)
                    scheduler.dismissDoseNotification(reminderScheduledAt)
                    scheduler.showLowStockNotifications(repository.snapshot.value.medicines)
                    finishReminder(reminderScheduledAt)
                },
                onSnooze = {
                    scheduler.dismissDoseNotification(reminderScheduledAt)
                    scheduler.scheduleSnoozed(reminderScheduledAt)
                    finishReminder(reminderScheduledAt)
                },
                onDismiss = {
                    finishReminder(reminderScheduledAt)
                },
            )
        }
    }
}
