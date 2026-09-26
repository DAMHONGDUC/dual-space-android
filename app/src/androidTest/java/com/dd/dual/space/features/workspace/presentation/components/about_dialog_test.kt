package com.dd.dual.space.features.workspace.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dd.dual.space.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun explainsUsageAndCloses() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var dismissed = false

        composeRule.setContent {
            MaterialTheme {
                aboutDialog(onDismiss = { dismissed = true })
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.about_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.about_step_add)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.got_it)).performClick()

        assertTrue(dismissed)
    }
}
