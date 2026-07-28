package com.denisp.pillstracker.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AppScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Xs),
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        subtitle?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        action?.invoke()
    }
}

@Composable
fun AppSurfaceCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.35f
    Card(
        modifier = modifier.then(
            if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
        ),
        shape = RoundedCornerShape(AppRadii.Card),
        colors = CardDefaults.cardColors(
            containerColor = containerColor ?: if (elevated) {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (dark) AppElevation.Field else {
                if (elevated) AppElevation.Interactive else AppElevation.Surface
            },
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = if (dark) 0.68f else 0.42f,
            ),
        ),
    ) {
        content()
    }
}

@Composable
fun AppDashboardCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable () -> Unit,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.35f
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AppRadii.Dashboard),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (dark) AppElevation.Field else AppElevation.Surface,
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (dark) 0.72f else 0.36f),
        ),
    ) {
        content()
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let {
            {
                Icon(imageVector = it, contentDescription = null)
            }
        },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        isError = isError,
        supportingText = supportingText,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(AppRadii.Control),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    )
}

@Composable
fun AppPickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    AppSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        elevated = true,
        onClick = if (enabled) onClick else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.Lg, vertical = AppSpacing.Md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Md),
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun AppPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(AppRadii.Control),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = AppElevation.Field,
            pressedElevation = 0.dp,
        ),
        content = content,
    )
}

@Composable
fun AppSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(AppRadii.Control),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
        content = content,
    )
}

@Composable
fun AppEmptyState(
    title: String,
    supportingText: String,
    modifier: Modifier = Modifier,
) {
    AppSurfaceCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.Xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = supportingText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
