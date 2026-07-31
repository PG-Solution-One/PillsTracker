package com.denisp.pillstracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.denisp.pillstracker.model.IntakeStatus
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduledDose
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.asTime
import com.denisp.pillstracker.ui.theme.AppStatusColors
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableIntakeCard(
    dose: ScheduledDose,
    canEdit: Boolean,
    onStatus: (IntakeStatus) -> Unit,
    modifier: Modifier = Modifier,
    showScheduledTime: Boolean = true,
    isNext: Boolean = false,
    onClick: (() -> Unit)? = null,
    embedded: Boolean = false,
    medicineAppearanceSize: Dp = 32.dp,
    prominentScheduledTime: Boolean = false,
) {
    val canTake = canEdit
    val currentOnStatus by rememberUpdatedState(onStatus)
    val currentCanEdit by rememberUpdatedState(canEdit)
    val currentCanTake by rememberUpdatedState(canTake)
    val density = LocalDensity.current
    val thresholdPx = with(density) { 64.dp.toPx() }
    val maximumOffsetPx = with(density) { 104.dp.toPx() }
    val haptics = LocalHapticFeedback.current
    var dragOffsetPx by remember(dose.medicine.id, dose.scheduledAt) {
        mutableFloatStateOf(0f)
    }
    var hapticTriggered by remember(dose.medicine.id, dose.scheduledAt) {
        mutableStateOf(false)
    }
    val dragState = rememberDraggableState { delta ->
        val minimum = if (currentCanEdit) -maximumOffsetPx else 0f
        val maximum = if (currentCanTake) maximumOffsetPx else 0f
        dragOffsetPx = (dragOffsetPx + delta).coerceIn(minimum, maximum)
        if (!hapticTriggered && abs(dragOffsetPx) >= thresholdPx) {
            hapticTriggered = true
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    val cardShape = if (embedded) RectangleShape else RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape),
    ) {
        if (dragOffsetPx != 0f) {
            val isTaken = dragOffsetPx > 0f
            val progress = (abs(dragOffsetPx) / thresholdPx).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 8.dp),
                contentAlignment = if (isTaken) Alignment.CenterStart else Alignment.CenterEnd,
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            alpha = 0.45f + progress * 0.55f
                            scaleX = 0.78f + progress * 0.22f
                            scaleY = scaleX
                        },
                    shape = CircleShape,
                    color = if (isTaken) {
                        AppStatusColors.Taken
                    } else {
                        AppStatusColors.Skipped
                    },
                    shadowElevation = 3.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isTaken) Icons.Rounded.Check else Icons.Rounded.Close,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
        AppSurfaceCard(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(dragOffsetPx.roundToInt(), 0) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = canEdit,
                    onDragStopped = {
                        val selectedStatus = when {
                            dragOffsetPx >= thresholdPx && currentCanTake -> IntakeStatus.TAKEN
                            dragOffsetPx <= -thresholdPx && currentCanEdit -> IntakeStatus.SKIPPED
                            else -> null
                        }
                        val returnAnimation = Animatable(dragOffsetPx)
                        returnAnimation.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        ) {
                            dragOffsetPx = value
                        }
                        dragOffsetPx = 0f
                        hapticTriggered = false
                        selectedStatus?.let(currentOnStatus)
                    },
            ),
            elevated = !embedded,
            containerColor = if (embedded) {
                MaterialTheme.colorScheme.surfaceContainerLowest
            } else {
                null
            },
            onClick = onClick,
            shape = cardShape,
            bordered = !embedded,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MedicineAppearance(
                        medicine = dose.medicine,
                        size = medicineAppearanceSize,
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(
                            text = dose.medicine.name,
                            style = if (prominentScheduledTime) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = buildString {
                                if (showScheduledTime && !prominentScheduledTime) {
                                    append(dose.scheduledAt.asTime())
                                    append(" · ")
                                }
                                append(dose.medicine.dosage)
                                append(" · ")
                                append(dose.medicine.tabletsPerIntake.displayAmount())
                                append(" шт.")
                                if (dose.medicine.scheduleKind == ScheduleKind.AS_NEEDED) {
                                    append(" · по необходимости")
                                }
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (prominentScheduledTime) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (dose.updatedAt != null && dose.status != IntakeStatus.PENDING) {
                            Text(
                                text = if (dose.status == IntakeStatus.TAKEN) {
                                    "Принято в ${dose.updatedAt.asTime()}"
                                } else {
                                    "Отмечено в ${dose.updatedAt.asTime()}"
                                },
                                color = if (dose.status == IntakeStatus.TAKEN) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                style = MaterialTheme.typography.labelMedium,
                            )
                        } else if (isNext && dose.status == IntakeStatus.PENDING) {
                            Text(
                                text = "Следующий приём",
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (showScheduledTime && prominentScheduledTime) {
                            ScheduledTimeBadge(scheduledAt = dose.scheduledAt)
                        }
                        IntakeStatusControls(
                            status = dose.status,
                            enabled = canEdit,
                            takenEnabled = canTake,
                            subjectName = dose.medicine.name,
                            onStatus = onStatus,
                        )
                    }
                }
            }
        }
    }
}
