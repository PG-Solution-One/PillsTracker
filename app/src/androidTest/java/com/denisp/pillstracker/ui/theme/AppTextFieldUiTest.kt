package com.denisp.pillstracker.ui.theme

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.text.input.ImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.denisp.pillstracker.model.ThemeMode
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppTextFieldUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun doneImeActionIsForwardedToCaller() {
        val done = AtomicBoolean(false)
        composeRule.setContent {
            PillsTrackerTheme(ThemeMode.LIGHT) {
                AppTextField(
                    value = "Витамин D",
                    onValueChange = {},
                    label = "Название лекарства",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { done.set(true) }),
                )
            }
        }

        composeRule.onNodeWithText("Витамин D").performImeAction()

        composeRule.runOnIdle {
            assertTrue(done.get())
        }
    }
}
