package com.denisp.pillstracker.ui.feature.settings

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.model.UserProfile
import com.denisp.pillstracker.ui.components.AgePickerField
import com.denisp.pillstracker.ui.components.ExactAlarmPermissionContent
import com.denisp.pillstracker.ui.components.ProfileDatePickerDialog
import com.denisp.pillstracker.ui.components.rememberExactAlarmPermissionState
import com.denisp.pillstracker.ui.theme.AppScreenHeader
import com.denisp.pillstracker.ui.theme.AppSpacing
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import com.denisp.pillstracker.ui.theme.AppTextField

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    userProfile: UserProfile,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onUserProfileChanged: (UserProfile) -> Unit,
) {
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showBirthDatePicker by remember { mutableStateOf(false) }
    val exactAlarmPermission = rememberExactAlarmPermissionState()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = AppSpacing.Screen,
                top = AppSpacing.Screen,
                end = AppSpacing.Screen,
                bottom = if (isImeVisible) AppSpacing.Sm else AppSpacing.Screen,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
        ) {
            item {
                AppScreenHeader("Настройки")
            }
            item {
                AppSurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.Xl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
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
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus(force = true)
                                    keyboardController?.hide()
                                },
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
                            .padding(AppSpacing.Xl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.Md),
                    ) {
                        Text("Оформление", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        ThemeModeSelector(
                            selectedMode = themeMode,
                            onModeSelected = onThemeModeChanged,
                        )
                    }
                }
            }
            if (exactAlarmPermission.isRequired) {
                item {
                    AppSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        ExactAlarmPermissionContent(
                            isGranted = exactAlarmPermission.isGranted,
                            onRequestPermission = exactAlarmPermission.openSettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.Xl),
                        )
                    }
                }
            }
            item {
                AppSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.Xl),
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
