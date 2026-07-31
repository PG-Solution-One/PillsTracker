package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.denisp.pillstracker.R
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.ui.theme.AppElevation
import com.denisp.pillstracker.ui.theme.AppPrimaryButton
import com.denisp.pillstracker.ui.theme.AppSecondaryButton
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard

@Composable
fun MedicineReminderOverlay(
    doses: List<ScheduledDose>,
    onStatus: (ScheduledDose, IntakeStatus) -> Unit,
    onTakeAll: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (doses.isEmpty()) return
    val multiple = doses.size > 1
    val singleDose = doses.singleOrNull()
    val notes = doses.map { it.medicine.note.trim() }.filter { it.isNotEmpty() }.distinct()

    Dialog(
        onDismissRequest = onDismiss,
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
                .widthIn(max = 520.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f),
            ),
            shadowElevation = AppElevation.Modal,
            tonalElevation = 3.dp,
        ) {
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

                if (multiple) {
                    GroupedIntakeCard(
                        doses = doses,
                        onStatus = onStatus,
                        onTakeAll = onTakeAll,
                        takeAllLabelOverride = stringResource(R.string.notification_taken_all),
                    )
                } else if (singleDose != null) {
                    SwipeableIntakeCard(
                        dose = singleDose,
                        canEdit = true,
                        onStatus = { status -> onStatus(singleDose, status) },
                        showScheduledTime = true,
                    )
                    Spacer(Modifier.height(AppSpacing.Xs))
                    AppPrimaryButton(
                        onClick = onTakeAll,
                        enabled = singleDose.status != IntakeStatus.TAKEN,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Text(
                            text = stringResource(
                                if (multiple) {
                                    R.string.notification_taken_all
                                } else {
                                    R.string.notification_taken_single
                                },
                            ),
                            modifier = Modifier.padding(start = AppSpacing.Sm),
                        )
                    }
                }

                AppSecondaryButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
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
        }
    }
}
