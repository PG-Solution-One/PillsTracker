package com.denisp.pillstracker.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class IntakeStatus {
    PENDING,
    TAKEN,
    SKIPPED,
}

data class IntakeRecord(
    val id: Long = 0,
    val medicineId: Long,
    val scheduledAt: Long,
    val status: IntakeStatus,
    val updatedAt: Long,
)

data class ScheduledDose(
    val medicine: Medicine,
    val scheduledAt: Long,
    val status: IntakeStatus,
) {
    val date: LocalDate
        get() = Instant.ofEpochMilli(scheduledAt).atZone(ZoneId.systemDefault()).toLocalDate()
}

data class TrackerSnapshot(
    val medicines: List<Medicine> = emptyList(),
    val records: List<IntakeRecord> = emptyList(),
)
