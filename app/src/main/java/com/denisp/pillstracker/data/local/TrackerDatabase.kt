package com.denisp.pillstracker.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.denisp.pillstracker.model.IntakeRecord
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.DEFAULT_MEDICINE_BACKGROUND_ARGB
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduleTime
import java.time.LocalDate

class TrackerDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE medicines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                form TEXT NOT NULL,
                pill_shape TEXT NOT NULL,
                color_argb INTEGER NOT NULL,
                secondary_color_argb INTEGER,
                background_color_argb INTEGER NOT NULL,
                dosage TEXT NOT NULL,
                dosage_amount REAL NOT NULL,
                dosage_unit TEXT NOT NULL,
                tablets_per_intake REAL NOT NULL,
                package_size REAL NOT NULL,
                remaining REAL NOT NULL,
                meal_timing TEXT NOT NULL,
                note TEXT NOT NULL,
                start_epoch_day INTEGER NOT NULL,
                end_epoch_day INTEGER,
                schedule_kind TEXT NOT NULL,
                state TEXT NOT NULL
            )
            """.trimIndent(),
        )
        database.execSQL(
            """
            CREATE TABLE schedule_times (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                medicine_id INTEGER NOT NULL,
                minute_of_day INTEGER NOT NULL,
                day_mask INTEGER NOT NULL,
                FOREIGN KEY(medicine_id) REFERENCES medicines(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        database.execSQL(
            """
            CREATE TABLE intake_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                medicine_id INTEGER NOT NULL,
                scheduled_at INTEGER NOT NULL,
                status TEXT NOT NULL,
                updated_at INTEGER NOT NULL,
                UNIQUE(medicine_id, scheduled_at),
                FOREIGN KEY(medicine_id) REFERENCES medicines(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        database.execSQL("CREATE INDEX idx_intakes_scheduled ON intake_records(scheduled_at)")
    }

    override fun onConfigure(database: SQLiteDatabase) {
        super.onConfigure(database)
        database.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            database.execSQL(
                "ALTER TABLE medicines ADD COLUMN pill_shape TEXT NOT NULL DEFAULT '${PillShape.ROUND.name}'",
            )
            database.execSQL("ALTER TABLE medicines ADD COLUMN secondary_color_argb INTEGER")
        }
        if (oldVersion < 3) {
            database.execSQL("ALTER TABLE medicines ADD COLUMN dosage_amount REAL NOT NULL DEFAULT 0")
            database.execSQL(
                "ALTER TABLE medicines ADD COLUMN dosage_unit TEXT NOT NULL DEFAULT '${DosageUnit.MG.name}'",
            )
            database.query("medicines", arrayOf("id", "dosage"), null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val dosage = cursor.getString(1)
                    val amount = dosage
                        .replace(',', '.')
                        .substringBefore(' ')
                        .toDoubleOrNull()
                        ?: 0.0
                    val unitText = dosage.substringAfter(' ', "").trim()
                    val unit = DosageUnit.entries.firstOrNull { it.title.equals(unitText, ignoreCase = true) }
                        ?: DosageUnit.MG
                    database.update(
                        "medicines",
                        ContentValues().apply {
                            put("dosage_amount", amount)
                            put("dosage_unit", unit.name)
                        },
                        "id = ?",
                        arrayOf(id.toString()),
                    )
                }
            }
        }
        if (oldVersion < 4) {
            database.execSQL(
                "ALTER TABLE medicines ADD COLUMN background_color_argb INTEGER NOT NULL " +
                    "DEFAULT $DEFAULT_MEDICINE_BACKGROUND_ARGB",
            )
        }
    }

    @Synchronized
    fun loadMedicines(): List<Medicine> {
        val database = readableDatabase
        val times = loadScheduleTimes(database).groupBy { it.medicineId }
        return database.query(
            "medicines",
            null,
            null,
            null,
            null,
            null,
            "name COLLATE NOCASE",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    add(
                        Medicine(
                            id = id,
                            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            form = enumValueOf(cursor.getString(cursor.getColumnIndexOrThrow("form"))),
                            pillShape = enumValueOf(
                                cursor.getString(cursor.getColumnIndexOrThrow("pill_shape")),
                            ),
                            colorArgb = cursor.getLong(cursor.getColumnIndexOrThrow("color_argb")),
                            secondaryColorArgb = cursor.getColumnIndexOrThrow("secondary_color_argb").let { index ->
                                if (cursor.isNull(index)) null else cursor.getLong(index)
                            },
                            backgroundColorArgb = cursor.getLong(
                                cursor.getColumnIndexOrThrow("background_color_argb"),
                            ),
                            dosageAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("dosage_amount")),
                            dosageUnit = enumValueOf(
                                cursor.getString(cursor.getColumnIndexOrThrow("dosage_unit")),
                            ),
                            tabletsPerIntake = cursor.getDouble(cursor.getColumnIndexOrThrow("tablets_per_intake")),
                            packageSize = cursor.getDouble(cursor.getColumnIndexOrThrow("package_size")),
                            remaining = cursor.getDouble(cursor.getColumnIndexOrThrow("remaining")),
                            mealTiming = enumValueOf(cursor.getString(cursor.getColumnIndexOrThrow("meal_timing"))),
                            note = cursor.getString(cursor.getColumnIndexOrThrow("note")),
                            startDate = LocalDate.ofEpochDay(
                                cursor.getLong(cursor.getColumnIndexOrThrow("start_epoch_day")),
                            ),
                            endDate = cursor.getColumnIndexOrThrow("end_epoch_day").let { index ->
                                if (cursor.isNull(index)) null else LocalDate.ofEpochDay(cursor.getLong(index))
                            },
                            scheduleKind = enumValueOf(
                                cursor.getString(cursor.getColumnIndexOrThrow("schedule_kind")),
                            ),
                            state = enumValueOf(cursor.getString(cursor.getColumnIndexOrThrow("state"))),
                            times = times[id].orEmpty(),
                        ),
                    )
                }
            }
        }
    }

    @Synchronized
    fun loadRecords(): List<IntakeRecord> =
        readableDatabase.query(
            "intake_records",
            null,
            null,
            null,
            null,
            null,
            "scheduled_at",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        IntakeRecord(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            medicineId = cursor.getLong(cursor.getColumnIndexOrThrow("medicine_id")),
                            scheduledAt = cursor.getLong(cursor.getColumnIndexOrThrow("scheduled_at")),
                            status = enumValueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
                            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")),
                        ),
                    )
                }
            }
        }

    @Synchronized
    fun saveMedicine(medicine: Medicine): Long {
        val database = writableDatabase
        database.beginTransaction()
        return try {
            val medicineId = if (medicine.id == 0L) {
                database.insertOrThrow("medicines", null, medicineValues(medicine))
            } else {
                database.update(
                    "medicines",
                    medicineValues(medicine),
                    "id = ?",
                    arrayOf(medicine.id.toString()),
                )
                medicine.id
            }
            database.delete("schedule_times", "medicine_id = ?", arrayOf(medicineId.toString()))
            medicine.times.forEach { schedule ->
                database.insertOrThrow(
                    "schedule_times",
                    null,
                    ContentValues().apply {
                        put("medicine_id", medicineId)
                        put("minute_of_day", schedule.minuteOfDay)
                        put("day_mask", schedule.dayMask)
                    },
                )
            }
            database.setTransactionSuccessful()
            medicineId
        } finally {
            database.endTransaction()
        }
    }

    @Synchronized
    fun updateMedicineState(medicineId: Long, state: MedicineState) {
        writableDatabase.update(
            "medicines",
            ContentValues().apply { put("state", state.name) },
            "id = ?",
            arrayOf(medicineId.toString()),
        )
    }

    @Synchronized
    fun deleteMedicine(medicineId: Long): Boolean =
        writableDatabase.delete(
            "medicines",
            "id = ?",
            arrayOf(medicineId.toString()),
        ) > 0

    @Synchronized
    fun addRemaining(medicineId: Long, addedAmount: Double) {
        if (!addedAmount.isFinite() || addedAmount <= 0.0) return

        writableDatabase.execSQL(
            """
            UPDATE medicines
            SET remaining = MAX(0, remaining) + ?
            WHERE id = ?
            """.trimIndent(),
            arrayOf<Any>(addedAmount, medicineId),
        )
    }

    @Synchronized
    fun markIntake(
        medicineId: Long,
        scheduledAt: Long,
        status: IntakeStatus,
        updatedAt: Long = System.currentTimeMillis(),
    ) {
        val database = writableDatabase
        database.beginTransaction()
        try {
            val previous = database.query(
                "intake_records",
                arrayOf("status"),
                "medicine_id = ? AND scheduled_at = ?",
                arrayOf(medicineId.toString(), scheduledAt.toString()),
                null,
                null,
                null,
            ).use { cursor ->
                if (cursor.moveToFirst()) enumValueOf<IntakeStatus>(cursor.getString(0)) else IntakeStatus.PENDING
            }
            if (previous != IntakeStatus.TAKEN && status == IntakeStatus.TAKEN) {
                val hasStock = database.query(
                    "medicines",
                    arrayOf("remaining", "tablets_per_intake"),
                    "id = ?",
                    arrayOf(medicineId.toString()),
                    null,
                    null,
                    null,
                ).use { cursor ->
                    cursor.moveToFirst() && cursor.getDouble(0) + 0.000_001 >= cursor.getDouble(1)
                }
                if (!hasStock) {
                    database.setTransactionSuccessful()
                    return
                }
            }
            database.insertWithOnConflict(
                "intake_records",
                null,
                ContentValues().apply {
                    put("medicine_id", medicineId)
                    put("scheduled_at", scheduledAt)
                    put("status", status.name)
                    put("updated_at", updatedAt)
                },
                SQLiteDatabase.CONFLICT_REPLACE,
            )

            if (previous != status && (previous == IntakeStatus.TAKEN || status == IntakeStatus.TAKEN)) {
                val delta = when {
                    previous != IntakeStatus.TAKEN && status == IntakeStatus.TAKEN -> -1.0
                    previous == IntakeStatus.TAKEN && status != IntakeStatus.TAKEN -> 1.0
                    else -> 0.0
                }
                database.execSQL(
                    """
                    UPDATE medicines
                    SET remaining = MAX(0, remaining + tablets_per_intake * ?)
                    WHERE id = ?
                    """.trimIndent(),
                    arrayOf<Any>(delta, medicineId),
                )
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    private fun loadScheduleTimes(database: SQLiteDatabase): List<ScheduleTime> =
        database.query("schedule_times", null, null, null, null, null, "minute_of_day").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        ScheduleTime(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            medicineId = cursor.getLong(cursor.getColumnIndexOrThrow("medicine_id")),
                            minuteOfDay = cursor.getInt(cursor.getColumnIndexOrThrow("minute_of_day")),
                            dayMask = cursor.getInt(cursor.getColumnIndexOrThrow("day_mask")),
                        ),
                    )
                }
            }
        }

    private fun medicineValues(medicine: Medicine) = ContentValues().apply {
        put("name", medicine.name.trim())
        put("form", medicine.form.name)
        put("pill_shape", medicine.pillShape.name)
        put("color_argb", medicine.colorArgb)
        if (medicine.secondaryColorArgb == null) {
            putNull("secondary_color_argb")
        } else {
            put("secondary_color_argb", medicine.secondaryColorArgb)
        }
        put("background_color_argb", medicine.backgroundColorArgb)
        put("dosage", medicine.dosage)
        put("dosage_amount", medicine.dosageAmount)
        put("dosage_unit", medicine.dosageUnit.name)
        put("tablets_per_intake", medicine.tabletsPerIntake)
        put("package_size", medicine.packageSize)
        put("remaining", medicine.remaining)
        put("meal_timing", medicine.mealTiming.name)
        put("note", medicine.note.trim())
        put("start_epoch_day", medicine.startDate.toEpochDay())
        if (medicine.endDate == null) putNull("end_epoch_day") else put("end_epoch_day", medicine.endDate.toEpochDay())
        put("schedule_kind", medicine.scheduleKind.name)
        put("state", medicine.state.name)
    }

    companion object {
        private const val DATABASE_NAME = "pills_tracker.db"
        private const val DATABASE_VERSION = 4
    }
}
