package com.denisp.pillstracker.ui.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.UserProfile
import com.denisp.pillstracker.ui.components.AgePickerField
import com.denisp.pillstracker.ui.components.ProfileDatePickerDialog
import com.denisp.pillstracker.ui.theme.AppPrimaryButton
import com.denisp.pillstracker.ui.theme.AppScreenHeader
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import com.denisp.pillstracker.ui.theme.AppTextField

@Composable
fun OnboardingScreen(
    initialProfile: UserProfile,
    onComplete: (UserProfile) -> Unit,
) {
    var name by remember(initialProfile.name) { mutableStateOf(initialProfile.name) }
    var birthDate by remember(initialProfile.birthDate) {
        mutableStateOf(initialProfile.birthDate)
    }
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            AppScreenHeader(
                title = "Добро пожаловать!",
                subtitle = "Настроим профиль, чтобы приложение обращалось к вам по имени.",
            )
            Spacer(Modifier.height(28.dp))
            AppSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                elevated = true,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AppTextField(
                        value = name,
                        onValueChange = { name = it.take(30) },
                        label = "Имя",
                        placeholder = "Как к вам обращаться?",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                        ),
                    )
                    AgePickerField(
                        birthDate = birthDate,
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        optional = true,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null)
                    Text(
                        text = "Ваши данные остаются на устройстве",
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Профиль, лекарства и история не отправляются в интернет.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            AppPrimaryButton(
                onClick = {
                    onComplete(
                        UserProfile(
                            name = name.trim(),
                            birthDate = birthDate,
                            onboardingCompleted = true,
                        ),
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Начать")
            }
        }
    }

    if (showDatePicker) {
        ProfileDatePickerDialog(
            selectedDate = birthDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                birthDate = it
                showDatePicker = false
            },
            onClear = {
                birthDate = null
                showDatePicker = false
            },
        )
    }
}
