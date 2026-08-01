package com.denisp.pillstracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.model.UserProfile
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.ui.components.ExactAlarmPermissionNoticeDialog
import com.denisp.pillstracker.ui.components.MedicineReminderOverlay
import com.denisp.pillstracker.ui.components.rememberExactAlarmPermissionState
import com.denisp.pillstracker.ui.components.updateIntakeGroupStatus
import com.denisp.pillstracker.ui.components.updateIntakeStatus
import com.denisp.pillstracker.ui.feature.editor.MedicineEditorScreen
import com.denisp.pillstracker.ui.feature.history.HistoryScreen
import com.denisp.pillstracker.ui.feature.medicines.MedicineDetailsScreen
import com.denisp.pillstracker.ui.feature.medicines.MedicinesScreen
import com.denisp.pillstracker.ui.feature.onboarding.OnboardingScreen
import com.denisp.pillstracker.ui.feature.settings.SettingsScreen
import com.denisp.pillstracker.ui.feature.today.TodayScreen
import java.time.LocalDate

private sealed interface AppDestination {
    data class Section(val section: MainSection) : AppDestination
    data class MedicineDetails(val medicineId: Long) : AppDestination
    data object MedicineEditor : AppDestination
}

@Composable
fun PillsTrackerApp(
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
    openedScheduledAt: Long?,
    onNotificationHandled: () -> Unit,
    themeMode: ThemeMode,
    userProfile: UserProfile,
    showExactAlarmNotice: Boolean,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onUserProfileChanged: (UserProfile) -> Unit,
    onExactAlarmNoticeSeen: () -> Unit,
) {
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    val snapshot by repository.snapshot.collectAsStateWithLifecycle()
    val navigationStack = remember {
        mutableStateListOf<AppDestination>(
            AppDestination.Section(MainSection.TODAY),
        )
    }
    val destination = navigationStack.last()
    var editedMedicine by remember { mutableStateOf<Medicine?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val reminderQueue = remember { mutableStateListOf<Long>() }
    val dismissedReminderTimestamps = remember { mutableStateListOf<Long>() }
    val exactAlarmPermission = rememberExactAlarmPermissionState()
    val navigate: (AppDestination) -> Unit = { target ->
        if (navigationStack.lastOrNull() != target) {
            navigationStack.add(target)
        }
    }
    val navigateBack: () -> Unit = {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.lastIndex)
        }
    }
    val showMedicines: (Long?) -> Unit = { medicineId ->
        if (medicineId == null) {
            navigate(AppDestination.Section(MainSection.MEDICINES))
        } else {
            navigate(AppDestination.MedicineDetails(medicineId))
        }
    }

    val enqueueReminder: (Long, Boolean) -> Unit = { scheduledAt, prioritize ->
        if (scheduledAt >= 0) {
            reminderQueue.remove(scheduledAt)
            if (prioritize) {
                reminderQueue.add(0, scheduledAt)
            } else {
                reminderQueue.add(scheduledAt)
                reminderQueue.sort()
            }
        }
    }
    val finishReminder: (Long) -> Unit = { scheduledAt ->
        reminderQueue.remove(scheduledAt)
        if (openedScheduledAt == scheduledAt) onNotificationHandled()
    }

    LaunchedEffect(
        showExactAlarmNotice,
        exactAlarmPermission.isRequired,
        exactAlarmPermission.isGranted,
    ) {
        if (
            showExactAlarmNotice &&
            (!exactAlarmPermission.isRequired || exactAlarmPermission.isGranted)
        ) {
            onExactAlarmNoticeSeen()
        }
    }

    LaunchedEffect(scheduler, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            scheduler.activeReminderTimestamps().forEach {
                if (it !in dismissedReminderTimestamps) enqueueReminder(it, false)
            }
            scheduler.reminderEvents.collect {
                dismissedReminderTimestamps.remove(it)
                enqueueReminder(it, false)
            }
        }
    }
    LaunchedEffect(openedScheduledAt) {
        openedScheduledAt?.let {
            dismissedReminderTimestamps.remove(it)
            enqueueReminder(it, true)
        }
    }

    val reminderScheduledAt = reminderQueue.firstOrNull()
    val reminderDoses = reminderScheduledAt
        ?.let(repository::dosesAt)
        .orEmpty()
    val pendingReminderDoses = reminderDoses.filter { it.status == IntakeStatus.PENDING }

    LaunchedEffect(
        reminderScheduledAt,
        pendingReminderDoses.map { "${it.medicine.id}:${it.status}" },
    ) {
        if (reminderScheduledAt != null && pendingReminderDoses.isEmpty()) {
            dismissedReminderTimestamps.remove(reminderScheduledAt)
            finishReminder(reminderScheduledAt)
        }
    }

    val detailsDestination = destination as? AppDestination.MedicineDetails
    val openedMedicine = detailsDestination?.let { details ->
        snapshot.medicines.firstOrNull { it.id == details.medicineId }
    }

    LaunchedEffect(detailsDestination, openedMedicine) {
        if (detailsDestination != null && openedMedicine == null) {
            navigateBack()
        }
    }

    BackHandler(
        enabled =
            (userProfile.onboardingCompleted || snapshot.medicines.isNotEmpty()) &&
                navigationStack.size > 1,
    ) {
        navigateBack()
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
                    onExactAlarmNoticeSeen = onExactAlarmNoticeSeen,
                )
            }

            destination == AppDestination.MedicineEditor -> {
                MedicineEditorScreen(
                    initialMedicine = editedMedicine,
                    onBack = navigateBack,
                    onSave = { medicine ->
                        repository.saveMedicine(medicine)
                        if (!medicine.trackStock && medicine.id != 0L) {
                            scheduler.dismissStockNotification(medicine.id)
                        }
                        scheduler.rescheduleAll()
                        navigateBack()
                    },
                )
            }

            destination is AppDestination.MedicineDetails && openedMedicine != null -> {
                MedicineDetailsScreen(
                    medicine = openedMedicine,
                    todayDoses = repository.dosesForDate(
                        date = LocalDate.now(),
                        activeOnly = false,
                    ).filter { it.medicine.id == openedMedicine.id },
                    onBack = navigateBack,
                    onEdit = {
                        editedMedicine = openedMedicine
                        navigate(AppDestination.MedicineEditor)
                    },
                )
            }

            destination is AppDestination.Section -> {
                val section = destination.section
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (!isImeVisible) {
                            MainNavigationBar(
                                selectedSection = section,
                                onSectionSelected = { item ->
                                    if (section != item) {
                                        navigate(AppDestination.Section(item))
                                    }
                                },
                            )
                        }
                    },
                    floatingActionButton = {
                        if (
                            !isImeVisible && (
                                section == MainSection.TODAY ||
                                    section == MainSection.MEDICINES
                                )
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    editedMedicine = null
                                    navigate(AppDestination.MedicineEditor)
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    },
                ) { padding ->
                    Box(
                        Modifier
                            .padding(padding)
                            .imePadding(),
                    ) {
                        when (section) {
                            MainSection.TODAY -> TodayScreen(
                                snapshot = snapshot,
                                repository = repository,
                                scheduler = scheduler,
                                userName = userProfile.name,
                                onShowMedicines = { showMedicines(null) },
                                onOpenMedicine = { showMedicines(it.id) },
                                onEditMedicine = {
                                    editedMedicine = it
                                    navigate(AppDestination.MedicineEditor)
                                },
                            )

                            MainSection.MEDICINES -> MedicinesScreen(
                                snapshot = snapshot,
                                repository = repository,
                                scheduler = scheduler,
                                onOpenDetails = {
                                    navigate(AppDestination.MedicineDetails(it.id))
                                },
                                onEdit = {
                                    editedMedicine = it
                                    navigate(AppDestination.MedicineEditor)
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

            else -> Unit
        }

        if (reminderScheduledAt != null && reminderDoses.isNotEmpty()) {
            MedicineReminderOverlay(
                doses = reminderDoses,
                onStatus = { dose, status ->
                    updateIntakeStatus(repository, scheduler, dose, status)
                    if (repository.dosesAt(reminderScheduledAt).any {
                            it.status == IntakeStatus.PENDING
                        }
                    ) {
                        scheduler.showDoseNotification(reminderScheduledAt)
                    } else {
                        finishReminder(reminderScheduledAt)
                    }
                },
                onTakeAll = {
                    updateIntakeGroupStatus(
                        repository = repository,
                        scheduler = scheduler,
                        scheduledAt = reminderScheduledAt,
                        status = IntakeStatus.TAKEN,
                    )
                    finishReminder(reminderScheduledAt)
                },
                onSnooze = {
                    scheduler.dismissDoseNotification(reminderScheduledAt)
                    scheduler.scheduleSnoozed(reminderScheduledAt)
                    finishReminder(reminderScheduledAt)
                },
                onDismiss = {
                    if (reminderScheduledAt !in dismissedReminderTimestamps) {
                        dismissedReminderTimestamps.add(reminderScheduledAt)
                    }
                    scheduler.dismissDoseNotification(reminderScheduledAt)
                    finishReminder(reminderScheduledAt)
                },
            )
        }

        if (
            showExactAlarmNotice &&
            userProfile.onboardingCompleted &&
            exactAlarmPermission.isRequired &&
            !exactAlarmPermission.isGranted &&
            reminderScheduledAt == null
        ) {
            ExactAlarmPermissionNoticeDialog(
                onDismiss = onExactAlarmNoticeSeen,
                onRequestPermission = {
                    onExactAlarmNoticeSeen()
                    exactAlarmPermission.openSettings()
                },
            )
        }
    }
}
