package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.ui.theme.AppElevation
import com.denisp.pillstracker.ui.theme.AppPrimaryButton
import com.denisp.pillstracker.ui.theme.AppSecondaryButton
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard

@Composable
internal fun StepHeader(currentStep: Int) {
    AppSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                editorSteps.indices.forEach { index ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                if (index <= currentStep) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                                CircleShape,
                            ),
                    )
                }
            }
            Text(
                "Шаг ${currentStep + 1} из ${editorSteps.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                editorSteps[currentStep].title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                editorSteps[currentStep].subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun EditorNavigation(
    currentStep: Int,
    stepsCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = AppElevation.Surface,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = AppSpacing.Screen, vertical = AppSpacing.Md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppSecondaryButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    enabled = currentStep > 0,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(AppSpacing.Sm))
                    Text("Назад")
                }
                AppPrimaryButton(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                ) {
                    Icon(
                        imageVector = if (currentStep == stepsCount - 1) {
                            Icons.Rounded.Check
                        } else {
                            Icons.AutoMirrored.Rounded.ArrowForward
                        },
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(AppSpacing.Sm))
                    Text(
                        if (currentStep == stepsCount - 1) {
                            "Сохранить"
                        } else {
                            "Далее"
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun EditSaveNavigation(
    enabled: Boolean,
    onSave: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = AppElevation.Surface,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            AppPrimaryButton(
                onClick = onSave,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = AppSpacing.Screen, vertical = AppSpacing.Md)
                    .heightIn(min = 52.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                )
                Spacer(Modifier.width(AppSpacing.Sm))
                Text("Сохранить изменения")
            }
        }
    }
}
