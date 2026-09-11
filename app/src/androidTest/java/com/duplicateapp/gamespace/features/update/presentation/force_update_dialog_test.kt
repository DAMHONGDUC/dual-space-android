package com.duplicateapp.gamespace.features.update.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duplicateapp.gamespace.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForceUpdateDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun requiredUpdateOffersUpdateOrCloseOnly() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var updateRequested = false

        composeRule.setContent {
            MaterialTheme { forceUpdateDialog(onUpdate = { updateRequested = true }, onClose = {}) }
        }

        composeRule.onNodeWithText(context.getString(R.string.update_required_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.update_now)).performClick()
        assertTrue(updateRequested)
    }
}
