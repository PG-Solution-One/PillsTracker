package com.denisp.pillstracker.ui.feature.medicines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.TrackerSnapshot
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.ui.theme.AppEmptyState
import com.denisp.pillstracker.ui.theme.AppScreenHeader

@Composable
fun MedicinesScreen(
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
    onOpenDetails: (Medicine) -> Unit,
    onEdit: (Medicine) -> Unit,
    onChanged: () -> Unit,
) {
    var selectedState by remember { mutableStateOf(MedicineState.ACTIVE) }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    val filtered = snapshot.medicines.filter { it.state == selectedState }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 100.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { AppScreenHeader("Мои лекарства") }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MedicineState.entries.forEach { state ->
                        FilterChip(
                            selected = selectedState == state,
                            onClick = { selectedState = state },
                            modifier = Modifier.weight(1f),
                            label = {
                                Text(
                                    text = state.filterTitle(),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            },
                        )
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    AppEmptyState(
                        title = "Здесь пока пусто",
                        supportingText = if (selectedState == MedicineState.ACTIVE) {
                            "Нажмите «+», чтобы добавить первое лекарство."
                        } else {
                            "Лекарства появятся здесь после изменения их статуса."
                        },
                    )
                }
            } else {
                items(filtered, key = { it.id }) { medicine ->
                    MedicineCatalogCard(
                        medicine = medicine,
                        onOpen = { onOpenDetails(medicine) },
                        onLongPress = { selectedMedicine = medicine },
                    )
                }
            }
        }
    }

    MedicineQuickActionsHost(
        selectedMedicine = selectedMedicine,
        repository = repository,
        scheduler = scheduler,
        onDismiss = { selectedMedicine = null },
        onEdit = onEdit,
        onChanged = onChanged,
    )
}

private fun MedicineState.filterTitle(): String = when (this) {
    MedicineState.ACTIVE -> "Активные"
    MedicineState.PAUSED -> "Пауза"
    MedicineState.ARCHIVED -> "Архив"
}
