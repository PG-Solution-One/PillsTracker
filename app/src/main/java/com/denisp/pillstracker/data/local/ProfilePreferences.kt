package com.denisp.pillstracker.data.local

import android.content.Context
import androidx.core.content.edit
import com.denisp.pillstracker.model.UserProfile
import java.time.LocalDate

class ProfilePreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PROFILE_PREFERENCES, Context.MODE_PRIVATE)
    private val legacyPreferences =
        context.getSharedPreferences(LEGACY_APPEARANCE_PREFERENCES, Context.MODE_PRIVATE)

    fun load(): UserProfile {
        val name = preferences.getString(KEY_NAME, null)
            ?: legacyPreferences.getString(LEGACY_KEY_USER_NAME, "").orEmpty()
        val birthDate = preferences.getString(KEY_BIRTH_DATE, null)
            ?.let { value -> runCatching { LocalDate.parse(value) }.getOrNull() }
        return UserProfile(
            name = name,
            birthDate = birthDate,
            onboardingCompleted = preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false),
        )
    }

    fun save(profile: UserProfile) {
        preferences.edit {
            putString(KEY_NAME, profile.name.trim())
            if (profile.birthDate == null) {
                remove(KEY_BIRTH_DATE)
            } else {
                putString(KEY_BIRTH_DATE, profile.birthDate.toString())
            }
            putBoolean(KEY_ONBOARDING_COMPLETED, profile.onboardingCompleted)
        }
    }

    fun hasSeenExactAlarmNotice(): Boolean =
        preferences.getBoolean(KEY_EXACT_ALARM_NOTICE_SEEN, false)

    fun markExactAlarmNoticeSeen() {
        preferences.edit { putBoolean(KEY_EXACT_ALARM_NOTICE_SEEN, true) }
    }

    companion object {
        private const val PROFILE_PREFERENCES = "profile"
        private const val KEY_NAME = "name"
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_EXACT_ALARM_NOTICE_SEEN = "exact_alarm_notice_seen"
        private const val LEGACY_APPEARANCE_PREFERENCES = "appearance"
        private const val LEGACY_KEY_USER_NAME = "user_name"
    }
}
