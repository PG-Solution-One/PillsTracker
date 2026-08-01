package com.denisp.pillstracker.ui.components

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.denisp.pillstracker.model.InterfaceMode
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.ui.theme.PillsTrackerTheme
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InterfaceModeToggleUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun standardModeCanBeChangedToSimplified() {
        val selectedMode = AtomicReference<InterfaceMode>()
        composeRule.setContent {
            PillsTrackerTheme(
                themeMode = ThemeMode.LIGHT,
                interfaceMode = InterfaceMode.STANDARD,
            ) {
                InterfaceModeToggle(
                    interfaceMode = InterfaceMode.STANDARD,
                    onInterfaceModeChanged = selectedMode::set,
                )
            }
        }

        composeRule.onNodeWithTag("interface_mode_toggle")
            .assertIsOff()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(InterfaceMode.SIMPLIFIED, selectedMode.get())
        }
    }
}
