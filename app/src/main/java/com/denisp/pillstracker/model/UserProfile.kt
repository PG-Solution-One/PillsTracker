package com.denisp.pillstracker.model

import java.time.LocalDate

data class UserProfile(
    val name: String = "",
    val birthDate: LocalDate? = null,
    val onboardingCompleted: Boolean = false,
)
