package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.denisp.pillstracker.R
import com.denisp.pillstracker.model.InterfaceMode
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.ui.theme.AppElevation
import com.denisp.pillstracker.ui.theme.AppPrimaryButton
import com.denisp.pillstracker.ui.theme.AppSecondaryButton
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import com.denisp.pillstracker.ui.theme.LocalInterfaceMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MedicineReminderOverlay(
    doses: List<ScheduledDose>,
    onStatus: (ScheduledDose, IntakeStatus) -> Unit,
    onTakeAll: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (doses.isEmpty()) return
    val simplified = LocalInterfaceMode.current == InterfaceMode.SIMPLIFIED
    val multiple = doses.size > 1
    val singleDose = doses.singleOrNull()
    val notes = doses.map { it.medicine.note.trim() }.filter { it.isNotEmpty() }.distinct()
    val nowMillis = rememberMinuteNow()
    val scope = rememberCoroutineScope()
    var submitting by remember { mutableStateOf(false) }
    var feedbackStatus by remember { mutableStateOf<IntakeStatus?>(null) }
    var visualStatuses by remember { mutableStateOf<Map<Long, IntakeStatus>>(emptyMap()) }
    val visualDoses = doses.map { dose ->
        dose.copy(status = visualStatuses[dose.medicine.id] ?: dose.status)
    }

    fun submitWithFeedback(status: IntakeStatus, action: () -> Unit) {
        if (submitting) return
        submitting = true
        feedbackStatus = status
        scope.launch {
            delay(FEEDBACK_DURATION_MILLIS)
            action()
        }
    }

    fun updateDose(dose: ScheduledDose, status: IntakeStatus) {
        visualStatuses = visualStatuses + (dose.medicine.id to status)
        val resolvesLastPending = dose.status == IntakeStatus.PENDING &&
            status != IntakeStatus.PENDING &&
            doses.count { it.status == IntakeStatus.PENDING } == 1
        if (resolvesLastPending) {
            submitWithFeedback(status) { onStatus(dose, status) }
        } else {
            onStatus(dose, status)
        }
    }

    fun takeAll() {
        visualStatuses = visualStatuses + doses
            .filter { it.status == IntakeStatus.PENDING }
            .associate { it.medicine.id to IntakeStatus.TAKEN }
        submitWithFeedback(IntakeStatus.TAKEN, onTakeAll)
    }

    Dialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.Xl)
                .widthIn(max = 520.dp)
                .heightIn(max = 640.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f),
            ),
            shadowElevation = AppElevation.Modal,
            tonalElevation = 3.dp,
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.Xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = AppElevation.Surface,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Text(
                        text = stringResource(
                            if (multiple) R.string.take_medicines else R.string.take_medicine,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    feedbackStatus?.let { status ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = if (status == IntakeStatus.TAKEN) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            },
                        ) {
                            Text(
                                text = if (status == IntakeStatus.TAKEN) {
                                    "Отмечено как принято"
                                } else {
                                    "Отмечено как пропущено"
                                },
                                modifier = Modifier.padding(AppSpacing.Md),
                                color = if (status == IntakeStatus.TAKEN) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                },
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    if (multiple) {
                        GroupedIntakeCard(
                            doses = visualDoses,
                            onStatus = ::updateDose,
                            onTakeAll = ::takeAll,
                            takeAllLabelOverride = stringResource(R.string.notification_taken_all),
                            enabled = !submitting,
                            nowMillis = nowMillis,
                        )
                    } else if (singleDose != null) {
                        val visualDose = visualDoses.single()
                        SwipeableIntakeCard(
                            dose = visualDose,
                            canEdit = !submitting,
                            onStatus = { status -> updateDose(singleDose, status) },
                            showScheduledTime = true,
                            nowMillis = nowMillis,
                        )
                        Spacer(Modifier.height(AppSpacing.Xs))
                        if (!simplified) {
                            AppPrimaryButton(
                                onClick = ::takeAll,
                                enabled = !submitting && singleDose.status != IntakeStatus.TAKEN,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                                Text(
                                    text = stringResource(R.string.notification_taken_single),
                                    modifier = Modifier.padding(start = AppSpacing.Sm),
                                )
                            }
                        }
                    }

                    AppSecondaryButton(
                        onClick = onSnooze,
                        enabled = !submitting,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.Alarm, contentDescription = null)
                        Text(
                            text = stringResource(R.string.reminder_snooze),
                            modifier = Modifier.padding(start = AppSpacing.Sm),
                        )
                    }

                    if (notes.isNotEmpty()) {
                        AppSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = notes.joinToString("\n"),
                                modifier = Modifier.padding(AppSpacing.Lg),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    enabled = !submitting,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(AppSpacing.Sm),
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Закрыть напоминание")
                }
            }
        }
    }
}

private const val FEEDBACK_DURATION_MILLIS = 400L
