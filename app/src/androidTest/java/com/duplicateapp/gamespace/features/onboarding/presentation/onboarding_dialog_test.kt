package com.duplicateapp.gamespace.features.onboarding.presentation

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
class OnboardingDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun onboardingExplainsProfilesPrivacyAndContinues() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var continued = false

        composeRule.setContent {
            MaterialTheme {
                onboardingDialog(onContinue = { continued = true })
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.onboarding_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_profiles)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_privacy)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.continue_label)).performClick()

        assertTrue(continued)
    }
}
