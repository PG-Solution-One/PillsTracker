package com.denisp.pillstracker.ui.feature.today

import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.TrackerSnapshot

internal data class TodayUiState(
    val activeMedicines: List<Medicine>,
    val asNeededMedicines: List<Medicine>,
    val lowStockMedicines: List<Medicine>,
    val nextDose: ScheduledDose?,
    val takenToday: Int,
    val totalToday: Int,
)

internal fun buildTodayUiState(
    snapshot: TrackerSnapshot,
    doses: List<ScheduledDose>,
    nowMillis: Long,
): TodayUiState {
    val activeMedicines = snapshot.medicines.filter { it.state == MedicineState.ACTIVE }
    val nextDose = doses.firstOrNull {
        it.status == IntakeStatus.PENDING && it.scheduledAt >= nowMillis
    } ?: doses.firstOrNull { it.status == IntakeStatus.PENDING }

    return TodayUiState(
        activeMedicines = activeMedicines,
        asNeededMedicines = activeMedicines.filter {
            it.scheduleKind == ScheduleKind.AS_NEEDED
        },
        lowStockMedicines = activeMedicines.filter {
            it.remaining <= it.tabletsPerIntake * 3
        },
        nextDose = nextDose,
        takenToday = doses.count { it.status == IntakeStatus.TAKEN },
        totalToday = doses.size,
    )
}
