package com.denisp.pillstracker.ui.feature.today

import com.denisp.pillstracker.domain.StockRules
import com.denisp.pillstracker.domain.DoseTimingPolicy
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
    val doseGroups: List<TodayDoseGroup>,
    val nextDose: ScheduledDose?,
    val takenToday: Int,
    val totalToday: Int,
)

internal data class TodayDoseGroup(
    val scheduledAt: Long,
    val doses: List<ScheduledDose>,
)

internal fun groupTodayDoses(doses: List<ScheduledDose>): List<TodayDoseGroup> =
    doses
        .sortedBy { it.scheduledAt }
        .groupBy { it.scheduledAt }
        .map { (scheduledAt, groupedDoses) ->
            TodayDoseGroup(
                scheduledAt = scheduledAt,
                doses = groupedDoses,
            )
        }

internal fun buildTodayUiState(
    snapshot: TrackerSnapshot,
    doses: List<ScheduledDose>,
    nowMillis: Long,
): TodayUiState {
    val activeMedicines = snapshot.medicines.filter { it.state == MedicineState.ACTIVE }
    val nextDose = doses.firstOrNull {
        DoseTimingPolicy.isOverdue(it, nowMillis)
    } ?: doses.firstOrNull {
        it.status == IntakeStatus.PENDING && it.scheduledAt >= nowMillis
    } ?: doses.firstOrNull { it.status == IntakeStatus.PENDING }

    return TodayUiState(
        activeMedicines = activeMedicines,
        asNeededMedicines = activeMedicines.filter {
            it.scheduleKind == ScheduleKind.AS_NEEDED
        },
        lowStockMedicines = activeMedicines.filter(StockRules::isLowStock),
        doseGroups = groupTodayDoses(doses),
        nextDose = nextDose,
        takenToday = doses.count { it.status == IntakeStatus.TAKEN },
        totalToday = doses.size,
    )
}
