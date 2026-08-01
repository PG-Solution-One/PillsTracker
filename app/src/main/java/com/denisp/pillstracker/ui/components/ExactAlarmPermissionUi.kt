package com.denisp.pillstracker.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.denisp.pillstracker.notifications.ExactAlarmAccess
import com.denisp.pillstracker.ui.theme.AppSpacing

internal const val EXACT_ALARM_DESCRIPTION =
    "Приложение работает и без этого разрешения, но в режиме экономии заряда " +
        "Android может задерживать напоминания. Включите его, чтобы они приходили " +
        "точно в выбранное время."

internal data class ExactAlarmPermissionState(
    val isRequired: Boolean,
    val isGranted: Boolean,
    val openSettings: () -> Unit,
)

@Composable
internal fun rememberExactAlarmPermissionState(): ExactAlarmPermissionState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isGranted by remember(context) {
        mutableStateOf(ExactAlarmAccess.isGranted(context))
    }

    DisposableEffect(context, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = ExactAlarmAccess.isGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return ExactAlarmPermissionState(
        isRequired = ExactAlarmAccess.isRequired,
        isGranted = isGranted,
        openSettings = { openExactAlarmSettings(context) },
    )
}

@Composable
internal fun ExactAlarmPermissionContent(
    isGranted: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
    ) {
        Text(
            text = "Точные напоминания",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = if (isGranted) {
                "Включены. Напоминания смогут приходить точно в выбранное время."
            } else {
                EXACT_ALARM_DESCRIPTION
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!isGranted) {
            Button(onClick = onRequestPermission) {
                Text("Включить точные напоминания")
            }
        }
    }
}

@Composable
internal fun ExactAlarmPermissionNoticeDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Alarm, contentDescription = null) },
        title = { Text("Точные напоминания") },
        text = { Text(EXACT_ALARM_DESCRIPTION) },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Включить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Не сейчас")
            }
        },
    )
}

private fun openExactAlarmSettings(context: Context) {
    try {
        context.startActivity(ExactAlarmAccess.settingsIntent(context))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
            },
        )
    }
}
