package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun StepHeader(currentStep: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

@Composable
internal fun EditorNavigation(
    currentStep: Int,
    stepsCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularArrowButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Предыдущий шаг",
                enabled = currentStep > 0,
                emphasized = false,
                onClick = onPrevious,
            )
            CircularArrowButton(
                icon = if (currentStep == stepsCount - 1) {
                    Icons.Rounded.Check
                } else {
                    Icons.AutoMirrored.Rounded.ArrowForward
                },
                contentDescription = if (currentStep == stepsCount - 1) {
                    "Сохранить лекарство"
                } else {
                    "Следующий шаг"
                },
                enabled = true,
                emphasized = true,
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun CircularArrowButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    emphasized: Boolean,
    onClick: () -> Unit,
) {
    val content: @Composable () -> Unit = {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(28.dp),
        )
    }
    if (emphasized) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(58.dp),
            shape = CircleShape,
            content = content,
        )
    } else {
        FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(58.dp),
            shape = CircleShape,
            content = content,
        )
    }
}
