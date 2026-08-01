package com.denisp.pillstracker.data.local

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.denisp.pillstracker.domain.ScheduleCalculator
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduleTime
import java.time.LocalDate
import java.time.ZoneId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackerDatabaseScheduleEditTest {
    private val zone = ZoneId.of("Europe/Moscow")
    private val date = LocalDate.of(2026, 8, 3)
    private lateinit var context: Context
    private lateinit var database: TrackerDatabase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DATABASE_NAME)
        database = TrackerDatabase(context)
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun newPastTimeDoesNotCreateTodayDose() {
        val savedAt = timestamp(10, 0)
        database.saveMedicine(
            medicine = medicine(time = 8 * 60),
            savedAt = savedAt,
            zoneId = zone,
        )

        val doses = ScheduleCalculator.dosesForDate(
            medicines = database.loadMedicines(),
            records = database.loadRecords(),
            date = date,
            zoneId = zone,
        )

        assertTrue(doses.isEmpty())
    }

    @Test
    fun editingAnInactivePastTimeDoesNotMakeItRetroactive() {
        val id = database.saveMedicine(
            medicine = medicine(time = 8 * 60),
            savedAt = timestamp(10, 0),
            zoneId = zone,
        )
        val original = database.loadMedicines().single { it.id == id }

        database.saveMedicine(
            medicine = original.copy(
                times = listOf(original.times.single().copy(minuteOfDay = 9 * 60)),
            ),
            savedAt = timestamp(10, 5),
            zoneId = zone,
        )

        val doses = ScheduleCalculator.dosesForDate(
            medicines = database.loadMedicines(),
            records = database.loadRecords(),
            date = date,
            zoneId = zone,
        )
        assertTrue(doses.isEmpty())
    }

    @Test
    fun editingTimeMovesTodayStatusWithoutChangingStockTwice() {
        val id = database.saveMedicine(
            medicine = medicine(time = 8 * 60),
            savedAt = date.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(),
            zoneId = zone,
        )
        val original = database.loadMedicines().single { it.id == id }
        val oldScheduledAt = timestamp(8, 0)
        val updatedAt = timestamp(8, 5)
        database.markIntake(id, oldScheduledAt, IntakeStatus.TAKEN, updatedAt)
        val remainingAfterTaking = database.loadMedicines().single { it.id == id }.remaining

        database.saveMedicine(
            medicine = original.copy(
                remaining = remainingAfterTaking,
                times = listOf(original.times.single().copy(minuteOfDay = 9 * 60)),
            ),
            savedAt = timestamp(10, 0),
            zoneId = zone,
        )

        val records = database.loadRecords()
        val edited = database.loadMedicines().single { it.id == id }
        val doses = ScheduleCalculator.dosesForDate(
            medicines = listOf(edited),
            records = records,
            date = date,
            zoneId = zone,
        )
        assertEquals(1, records.size)
        assertEquals(timestamp(9, 0), records.single().scheduledAt)
        assertEquals(IntakeStatus.TAKEN, records.single().status)
        assertEquals(updatedAt, records.single().updatedAt)
        assertEquals(remainingAfterTaking, edited.remaining, 0.0)
        assertEquals(listOf(timestamp(9, 0)), doses.map { it.scheduledAt })
        assertEquals(IntakeStatus.TAKEN, doses.single().status)
    }

    private fun medicine(time: Int) = Medicine(
        name = "Test",
        form = MedicineForm.TABLET,
        colorArgb = 0xFF147D64,
        dosageAmount = 100.0,
        dosageUnit = DosageUnit.MG,
        tabletsPerIntake = 1.0,
        packageSize = 30.0,
        remaining = 30.0,
        mealTiming = MealTiming.ANY,
        note = "",
        startDate = date.minusDays(1),
        endDate = null,
        scheduleKind = ScheduleKind.DAILY,
        times = listOf(ScheduleTime(minuteOfDay = time)),
    )

    private fun timestamp(hour: Int, minute: Int): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    private companion object {
        const val DATABASE_NAME = "pills_tracker.db"
    }
}
