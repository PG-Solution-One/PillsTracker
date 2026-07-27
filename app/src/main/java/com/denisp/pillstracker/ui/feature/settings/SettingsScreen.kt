package com.denisp.pillstracker.ui.feature.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.model.UserProfile
import com.denisp.pillstracker.ui.components.AgePickerField
import com.denisp.pillstracker.ui.components.ProfileDatePickerDialog
import com.denisp.pillstracker.ui.theme.AppScreenHeader
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import com.denisp.pillstracker.ui.theme.AppTextField

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    userProfile: UserProfile,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onUserProfileChanged: (UserProfile) -> Unit,
) {
    val context = LocalContext.current
    var showBirthDatePicker by remember { mutableStateOf(false) }
    val exactAlarmsAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    } else {
        true
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AppScreenHeader("Настройки")
            }
            item {
                AppSurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "Профиль",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Имя используется для приветствия. Данные профиля хранятся только на устройстве.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppTextField(
                            value = userProfile.name,
                            onValueChange = {
                                onUserProfileChanged(userProfile.copy(name = it.take(30)))
                            },
                            label = "Имя",
                            placeholder = "Как к вам обращаться?",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                            ),
                        )
                        AgePickerField(
                            birthDate = userProfile.birthDate,
                            onClick = { showBirthDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            item {
                AppSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("Оформление", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { onThemeModeChanged(mode) },
                                    modifier = Modifier.weight(1f),
                                    label = {
                                        Text(
                                            text = mode.title,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
            item {
                AppSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "Точные напоминания",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (exactAlarmsAvailable) {
                                "Разрешены. Уведомления смогут приходить точно в назначенное время."
                            } else {
                                "Android ограничивает точные будильники. Разрешите их для надёжных напоминаний."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (!exactAlarmsAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = "package:${context.packageName}".toUri()
                                        },
                                    )
                                },
                            ) {
                                Text("Разрешить")
                            }
                        }
                    }
                }
            }
            item {
                AppSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                    ) {
                        Text("Pills Tracker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Все данные хранятся только на этом устройстве.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showBirthDatePicker) {
        ProfileDatePickerDialog(
            selectedDate = userProfile.birthDate,
            onDismiss = { showBirthDatePicker = false },
            onDateSelected = {
                onUserProfileChanged(userProfile.copy(birthDate = it))
                showBirthDatePicker = false
            },
            onClear = {
                onUserProfileChanged(userProfile.copy(birthDate = null))
                showBirthDatePicker = false
            },
        )
    }
}
