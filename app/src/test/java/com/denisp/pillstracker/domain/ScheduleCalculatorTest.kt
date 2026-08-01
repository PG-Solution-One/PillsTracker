package com.denisp.pillstracker.domain

import com.denisp.pillstracker.model.ALL_DAYS_MASK
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.IntakeRecord
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduleTime
import com.denisp.pillstracker.model.dayMask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ScheduleCalculatorTest {
    @Test
    fun everyOtherDayStartsFromCourseStart() {
        val medicine = medicine(
            startDate = LocalDate.of(2026, 7, 20),
            scheduleKind = ScheduleKind.EVERY_OTHER_DAY,
        )

        assertTrue(ScheduleCalculator.isScheduledOn(medicine, LocalDate.of(2026, 7, 20)))
        assertFalse(ScheduleCalculator.isScheduledOn(medicine, LocalDate.of(2026, 7, 21)))
        assertTrue(ScheduleCalculator.isScheduledOn(medicine, LocalDate.of(2026, 7, 22)))
    }

    @Test
    fun selectedTimeOnlyAppearsOnItsDays() {
        val medicine = medicine(
            scheduleKind = ScheduleKind.SELECTED_DAYS,
            times = listOf(
                ScheduleTime(minuteOfDay = 8 * 60, dayMask = dayMask(1) or dayMask(3)),
            ),
        )

        val monday = ScheduleCalculator.dosesForDate(
            listOf(medicine),
            emptyList(),
            LocalDate.of(2026, 7, 20),
        )
        val tuesday = ScheduleCalculator.dosesForDate(
            listOf(medicine),
            emptyList(),
            LocalDate.of(2026, 7, 21),
        )

        assertEquals(1, monday.size)
        assertTrue(tuesday.isEmpty())
    }

    @Test
    fun courseEndDateIsInclusive() {
        val medicine = medicine(endDate = LocalDate.of(2026, 7, 25))

        assertTrue(ScheduleCalculator.isScheduledOn(medicine, LocalDate.of(2026, 7, 25)))
        assertFalse(ScheduleCalculator.isScheduledOn(medicine, LocalDate.of(2026, 7, 26)))
    }

    @Test
    fun multipleDailyTimesHaveIndependentStatuses() {
        val date = LocalDate.of(2026, 7, 27)
        val zone = ZoneId.of("Europe/Moscow")
        val morning = date.atTime(8, 0).atZone(zone).toInstant().toEpochMilli()
        val evening = date.atTime(20, 0).atZone(zone).toInstant().toEpochMilli()
        val medicine = medicine(
            times = listOf(
                ScheduleTime(minuteOfDay = 8 * 60, dayMask = ALL_DAYS_MASK),
                ScheduleTime(minuteOfDay = 20 * 60, dayMask = ALL_DAYS_MASK),
            ),
        )
        val records = listOf(
            IntakeRecord(
                medicineId = medicine.id,
                scheduledAt = morning,
                status = IntakeStatus.TAKEN,
                updatedAt = morning + 5 * 60 * 1000,
            ),
        )

        val doses = ScheduleCalculator.dosesForDate(
            medicines = listOf(medicine),
            records = records,
            date = date,
            zoneId = zone,
        )

        assertEquals(listOf(morning, evening), doses.map { it.scheduledAt })
        assertEquals(listOf(IntakeStatus.TAKEN, IntakeStatus.PENDING), doses.map { it.status })
    }

    @Test
    fun `dose before schedule activation is not created`() {
        val date = LocalDate.of(2026, 7, 27)
        val zone = ZoneId.of("Europe/Moscow")
        val scheduledAt = date.atTime(8, 0).atZone(zone).toInstant().toEpochMilli()
        val medicine = medicine(
            times = listOf(
                ScheduleTime(
                    minuteOfDay = 8 * 60,
                    effectiveFromMillis = scheduledAt + 2 * 60 * 60 * 1000,
                ),
            ),
        )

        val doses = ScheduleCalculator.dosesForDate(
            medicines = listOf(medicine),
            records = emptyList(),
            date = date,
            zoneId = zone,
        )

        assertTrue(doses.isEmpty())
    }

    @Test
    fun `first future occurrence after activation is created`() {
        val activationDate = LocalDate.of(2026, 7, 27)
        val nextDate = activationDate.plusDays(1)
        val zone = ZoneId.of("Europe/Moscow")
        val effectiveFrom = activationDate.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val medicine = medicine(
            times = listOf(
                ScheduleTime(
                    minuteOfDay = 8 * 60,
                    effectiveFromMillis = effectiveFrom,
                ),
            ),
        )

        val doses = ScheduleCalculator.dosesForDate(
            medicines = listOf(medicine),
            records = emptyList(),
            date = nextDate,
            zoneId = zone,
        )

        assertEquals(1, doses.size)
        assertEquals(
            nextDate.atTime(8, 0).atZone(zone).toInstant().toEpochMilli(),
            doses.single().scheduledAt,
        )
    }

    private fun medicine(
        startDate: LocalDate = LocalDate.of(2026, 7, 20),
        endDate: LocalDate? = null,
        scheduleKind: ScheduleKind = ScheduleKind.DAILY,
        times: List<ScheduleTime> = listOf(
            ScheduleTime(minuteOfDay = 8 * 60, dayMask = ALL_DAYS_MASK),
        ),
    ) = Medicine(
        id = 1,
        name = "Тест",
        form = MedicineForm.TABLET,
        colorArgb = 0xFF147D64,
        dosageAmount = 500.0,
        dosageUnit = DosageUnit.MG,
        tabletsPerIntake = 1.0,
        packageSize = 30.0,
        remaining = 30.0,
        mealTiming = MealTiming.ANY,
        note = "",
        startDate = startDate,
        endDate = endDate,
        scheduleKind = scheduleKind,
        times = times,
    )
}
