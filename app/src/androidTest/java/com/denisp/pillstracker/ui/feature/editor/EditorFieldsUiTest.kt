package com.denisp.pillstracker.ui.feature.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.denisp.pillstracker.model.ThemeMode
import com.denisp.pillstracker.ui.theme.PillsTrackerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorFieldsUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectionOptionsUseEqualTwoColumnGrid() {
        composeRule.setContent {
            PillsTrackerTheme(ThemeMode.LIGHT) {
                Box(Modifier.width(320.dp)) {
                    SelectionField(
                        label = "Связь с едой",
                        selected = "Неважно",
                        options = listOf("Неважно", "До еды", "Во время еды", "После еды"),
                        onSelected = {},
                        title = { it },
                        columns = 2,
                    )
                }
            }
        }

        val bounds = listOf("Неважно", "До еды", "Во время еды", "После еды").map {
            composeRule
                .onNodeWithTag("selection-option-$it")
                .fetchSemanticsNode()
                .boundsInRoot
        }

        bounds.drop(1).forEach { optionBounds ->
            assertEquals(bounds.first().width, optionBounds.width, 1.1f)
            assertEquals(bounds.first().height, optionBounds.height, 1.1f)
        }
        assertEquals(bounds[0].top, bounds[1].top, 0.5f)
        assertEquals(bounds[2].top, bounds[3].top, 0.5f)
        assertEquals(bounds[0].left, bounds[2].left, 0.5f)
    }
}
