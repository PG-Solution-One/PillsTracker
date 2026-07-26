package com.denisp.pillstracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.ui.screens.HistoryScreen
import com.denisp.pillstracker.ui.screens.MedicineEditorScreen
import com.denisp.pillstracker.ui.screens.MedicinesScreen
import com.denisp.pillstracker.ui.screens.SettingsScreen
import com.denisp.pillstracker.ui.screens.TodayScreen

private enum class MainSection(val title: String, val icon: ImageVector) {
    TODAY("Сегодня", Icons.Rounded.Today),
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
    onThemeModeChanged: (ThemeMode) -> Unit,
) {
    val snapshot by repository.snapshot.collectAsStateWithLifecycle()
    var section by remember { mutableStateOf(MainSection.TODAY) }
    var editedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var editorOpen by remember { mutableStateOf(false) }

    if (editorOpen) {
        MedicineEditorScreen(
            initialMedicine = editedMedicine,
            onBack = { editorOpen = false },
            onSave = { medicine ->
                repository.saveMedicine(medicine)
                scheduler.rescheduleAll()
                editorOpen = false
            },
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                MainSection.entries.forEach { item ->
                    NavigationBarItem(
                        selected = section == item,
                        onClick = { section = item },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(30.dp),
                            )
                        },
                        label = { Text(item.title) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (section == MainSection.TODAY || section == MainSection.MEDICINES) {
                FloatingActionButton(
                    onClick = {
                        editedMedicine = null
                        editorOpen = true
                    },
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
                    openedScheduledAt = openedScheduledAt,
                    onNotificationHandled = onNotificationHandled,
                )
                MainSection.MEDICINES -> MedicinesScreen(
                    medicines = snapshot.medicines,
                    repository = repository,
                    onEdit = {
                        editedMedicine = it
                        editorOpen = true
                    },
                    onChanged = scheduler::rescheduleAll,
                )
                MainSection.HISTORY -> HistoryScreen(snapshot, repository)
                MainSection.SETTINGS -> SettingsScreen(themeMode, onThemeModeChanged)
            }
        }
    }
}
