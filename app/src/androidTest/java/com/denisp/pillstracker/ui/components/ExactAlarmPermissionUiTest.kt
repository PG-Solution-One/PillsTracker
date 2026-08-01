package com.denisp.pillstracker.ui.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.ui.theme.PillsTrackerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExactAlarmPermissionUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun deniedStateExplainsDelayAndOffersSettings() {
        var requested = false
        composeRule.setContent {
            PillsTrackerTheme(ThemeMode.LIGHT) {
                ExactAlarmPermissionContent(
                    isGranted = false,
                    onRequestPermission = { requested = true },
                )
            }
        }

        composeRule.onNodeWithText(EXACT_ALARM_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithText("Включить точные напоминания").performClick()
        composeRule.runOnIdle { assertTrue(requested) }
    }

    @Test
    fun grantedStateShowsConfirmationWithoutButton() {
        composeRule.setContent {
            PillsTrackerTheme(ThemeMode.LIGHT) {
                ExactAlarmPermissionContent(
                    isGranted = true,
                    onRequestPermission = {},
                )
            }
        }

        composeRule
            .onNodeWithText("Включены. Напоминания смогут приходить точно в выбранное время.")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Включить точные напоминания").assertCountEquals(0)
    }
}
