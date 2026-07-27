package com.denisp.pillstracker.domain

import com.denisp.pillstracker.model.IntakeRecord
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.dayMask
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object ScheduleCalculator {
    fun dosesForDate(
        medicines: List<Medicine>,
        records: List<IntakeRecord>,
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
        activeOnly: Boolean = true,
    ): List<ScheduledDose> {
        val statuses = records.associateBy { it.medicineId to it.scheduledAt }
        return medicines
            .asSequence()
            .filter {
                (!activeOnly || it.state == MedicineState.ACTIVE) &&
                    it.scheduleKind != ScheduleKind.AS_NEEDED
            }
            .filter { isScheduledOn(it, date) }
            .flatMap { medicine ->
                medicine.times
                    .asSequence()
                    .filter { schedule -> schedule.dayMask and dayMask(date.dayOfWeek.value) != 0 }
                    .map { schedule ->
                        val time = date.atStartOfDay(zoneId).plusMinutes(schedule.minuteOfDay.toLong()).toInstant().toEpochMilli()
                        val record = statuses[medicine.id to time]
                        ScheduledDose(
                            medicine = medicine,
                            scheduledAt = time,
                            status = record?.status ?: IntakeStatus.PENDING,
                            updatedAt = record?.updatedAt,
                        )
                    }
            }
            .sortedBy { it.scheduledAt }
            .toList()
    }

    fun upcomingTimestamps(
        medicines: List<Medicine>,
        fromMillis: Long,
        days: Long = 30,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<Long> {
        val start = Instant.ofEpochMilli(fromMillis).atZone(zoneId).toLocalDate()
        return (0 until days)
            .asSequence()
            .flatMap { offset ->
                dosesForDate(medicines, emptyList(), start.plusDays(offset), zoneId).asSequence()
            }
            .map { it.scheduledAt }
            .filter { it > fromMillis }
            .distinct()
            .sorted()
            .toList()
    }

    fun isScheduledOn(medicine: Medicine, date: LocalDate): Boolean {
        if (date.isBefore(medicine.startDate)) return false
        if (medicine.endDate?.let(date::isAfter) == true) return false
        return when (medicine.scheduleKind) {
            ScheduleKind.DAILY, ScheduleKind.SELECTED_DAYS -> true
            ScheduleKind.EVERY_OTHER_DAY ->
                ChronoUnit.DAYS.between(medicine.startDate, date) % 2L == 0L
            ScheduleKind.AS_NEEDED -> false
        }
    }
}
