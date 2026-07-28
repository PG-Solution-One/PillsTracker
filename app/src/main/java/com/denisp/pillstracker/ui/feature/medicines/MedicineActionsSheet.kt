package com.denisp.pillstracker.ui.feature.medicines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.components.MedicineAppearance
import com.denisp.pillstracker.ui.theme.AppElevation
import com.denisp.pillstracker.ui.theme.AppPrimaryButton
import com.denisp.pillstracker.ui.theme.AppRadii
import com.denisp.pillstracker.ui.theme.AppSecondaryButton
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MedicineActionsSheet(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onRefill: () -> Unit,
    onStateChange: (MedicineState) -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = AppRadii.Dashboard,
            topEnd = AppRadii.Dashboard,
        ),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = AppElevation.Modal,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outline,
            )
        },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(
                        start = AppSpacing.Screen,
                        end = AppSpacing.Screen,
                        bottom = AppSpacing.Xxl,
                    ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
            ) {
                AppSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevated = true,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.Xl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
                        ) {
                            MedicineAppearance(medicine = medicine, size = 58.dp)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = medicine.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "${medicine.form.title} · ${medicine.dosage}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        MedicineDetail(
                            label = "Расписание",
                            value = medicineScheduleSummary(medicine),
                        )
                        MedicineDetail(
                            label = "Остаток",
                            value = "${medicine.remaining.displayAmount()} из " +
                                "${medicine.packageSize.displayAmount()} шт.",
                        )
                        if (medicine.note.isNotBlank()) {
                            MedicineDetail(
                                label = "Заметка",
                                value = medicine.note,
                            )
                        }
                    }
                }

                Text(
                    text = "Действия",
                    modifier = Modifier.padding(top = AppSpacing.Sm),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                AppPrimaryButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                    )
                    Text(
                        text = "Редактировать",
                        modifier = Modifier.padding(start = AppSpacing.Sm),
                    )
                }
                AppSecondaryButton(
                    onClick = onRefill,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                ) {
                    Text("Пополнить остаток")
                }
                MedicineStateActions(
                    state = medicine.state,
                    onStateChange = onStateChange,
                )
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(top = AppSpacing.Sm),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteForever,
                        contentDescription = null,
                    )
                    Text(
                        text = "Удалить лекарство",
                        modifier = Modifier.padding(start = AppSpacing.Sm),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
@Composable
private fun MedicineDetail(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
@Composable
private fun MedicineStateActions(
    state: MedicineState,
    onStateChange: (MedicineState) -> Unit,
) {
    when (state) {
        MedicineState.ACTIVE -> {
            AppSecondaryButton(
                onClick = { onStateChange(MedicineState.PAUSED) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                Text("Приостановить")
            }
            TextButton(
                onClick = { onStateChange(MedicineState.ARCHIVED) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Завершить курс")
            }
        }

        MedicineState.PAUSED -> {
            AppSecondaryButton(
                onClick = { onStateChange(MedicineState.ACTIVE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                Text("Возобновить")
            }
            TextButton(
                onClick = { onStateChange(MedicineState.ARCHIVED) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Перенести в архив")
            }
        }

        MedicineState.ARCHIVED -> {
            AppSecondaryButton(
                onClick = { onStateChange(MedicineState.ACTIVE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                Text("Возобновить курс")
            }
        }
    }
}
