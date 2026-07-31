package com.denisp.pillstracker.ui.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.R
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.TrackerSnapshot
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.ui.components.GroupedIntakeCard
import com.denisp.pillstracker.ui.components.updateIntakeGroupStatus
import com.denisp.pillstracker.ui.components.updateIntakeStatus
import com.denisp.pillstracker.ui.components.rememberMinuteNow
import com.denisp.pillstracker.ui.feature.medicines.MedicineQuickActionsHost
import com.denisp.pillstracker.ui.theme.AppEmptyState
import com.denisp.pillstracker.ui.theme.AppSectionHeader
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TodayScreen(
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
    userName: String,
    onShowMedicines: () -> Unit,
    onOpenMedicine: (Medicine) -> Unit,
    onEditMedicine: (Medicine) -> Unit,
) {
    val today = LocalDate.now()
    val nowMillis = rememberMinuteNow()
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    val doses = repository.dosesForDateIncludingManual(today, activeOnly = true)
    val uiState = buildTodayUiState(
        snapshot = snapshot,
        doses = doses,
        nowMillis = nowMillis,
    )
    val markDose: (ScheduledDose, IntakeStatus) -> Unit = { dose, status ->
        updateIntakeStatus(repository, scheduler, dose, status)
    }

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
            item {
                TodayGreetingHeader(
                    userName = userName,
                    date = today,
                    period = greetingPeriodForHour(LocalTime.now().hour),
                )
            }

            item {
                TodayOverviewCard(
                    activeMedicineCount = uiState.activeMedicines.size,
                    nextDose = uiState.nextDose,
                    takenToday = uiState.takenToday,
                    totalToday = uiState.totalToday,
                    nowMillis = nowMillis,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppSectionHeader(
                        title = stringResource(R.string.my_medicines),
                        supportingText = "${uiState.takenToday} из ${uiState.totalToday} принято",
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onShowMedicines) { Text("Все") }
                }
            }

            if (doses.isEmpty()) {
                item {
                    AppEmptyState(
                        title = "Запланированных приёмов нет",
                        supportingText = if (uiState.activeMedicines.isEmpty()) {
                            "Добавьте первое лекарство кнопкой «+»."
                        } else {
                            "На сегодня в расписании ничего не запланировано."
                        },
                    )
                }
            } else {
                items(
                    uiState.doseGroups,
                    key = { "today-group-${it.scheduledAt}" },
                ) { group ->
                    if (group.doses.size == 1) {
                        val dose = group.doses.first()
                        TodayDoseCard(
                            dose = dose,
                            isNext = uiState.nextDose?.medicine?.id == dose.medicine.id &&
                                uiState.nextDose.scheduledAt == dose.scheduledAt,
                            onStatus = { markDose(dose, it) },
                            onOpen = { onOpenMedicine(dose.medicine) },
                            onLongPress = { selectedMedicine = dose.medicine },
                            nowMillis = nowMillis,
                        )
                    } else {
                        GroupedIntakeCard(
                            doses = group.doses,
                            isNext = uiState.nextDose?.scheduledAt == group.scheduledAt,
                            onStatus = markDose,
                            onTakeAll = {
                                updateIntakeGroupStatus(
                                    repository = repository,
                                    scheduler = scheduler,
                                    scheduledAt = group.scheduledAt,
                                    status = IntakeStatus.TAKEN,
                                )
                            },
                            onOpen = onOpenMedicine,
                            onLongPress = { selectedMedicine = it },
                            medicineAppearanceSize = 56.dp,
                            prominentTime = true,
                            nowMillis = nowMillis,
                        )
                    }
                }
            }

            if (uiState.asNeededMedicines.isNotEmpty()) {
                item { TodaySectionTitle("По необходимости") }
                items(uiState.asNeededMedicines, key = { "as-needed-${it.id}" }) { medicine ->
                    AsNeededMedicineCard(
                        medicine = medicine,
                        onTaken = {
                            val now = System.currentTimeMillis() / 60_000L * 60_000L
                            repository.markIntake(medicine.id, now, IntakeStatus.TAKEN)
                            scheduler.showLowStockNotifications(
                                repository.snapshot.value.medicines.filter {
                                    it.id == medicine.id
                                },
                            )
                        },
                        onOpen = { onOpenMedicine(medicine) },
                        onLongPress = { selectedMedicine = medicine },
                    )
                }
            }

            if (uiState.lowStockMedicines.isNotEmpty()) {
                item { TodaySectionTitle("Заканчивается") }
                items(uiState.lowStockMedicines, key = { "stock-${it.id}" }) { medicine ->
                    LowStockMedicineCard(
                        medicine = medicine,
                        onOpen = { onOpenMedicine(medicine) },
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
        onEdit = onEditMedicine,
        onChanged = scheduler::rescheduleAll,
    )
}
