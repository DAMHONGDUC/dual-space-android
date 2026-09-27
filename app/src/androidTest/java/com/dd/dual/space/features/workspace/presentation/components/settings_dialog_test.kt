package com.dd.dual.space.features.workspace.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dd.dual.space.R
import com.dd.dual.space.features.settings.domain.AppLanguage
import com.dd.dual.space.features.settings.domain.ThemeMode
import com.dd.dual.space.features.workspace.domain.ProfileProvisioningStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun adConsentChoicesOpenWhenRequired() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var privacyOptionsOpened = false

        composeRule.setContent {
            MaterialTheme {
                settings(privacyOptionsRequired = true, onOpenPrivacyOptions = { privacyOptionsOpened = true })
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.privacy_options)).performScrollTo().performClick()
        assertTrue(privacyOptionsOpened)
    }

    @Test
    fun freeAppShowsNoAccountOrPurchaseControls() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MaterialTheme {
                settings(privacyOptionsRequired = false, onOpenPrivacyOptions = {})
            }
        }

        composeRule.onAllNodesWithText(context.getString(R.string.privacy_options)).assertCountEquals(0)
        composeRule.onAllNodesWithText("Premium", substring = true).assertCountEquals(0)
        composeRule.onAllNodesWithText("Google", substring = true).assertCountEquals(0)
    }

    @Composable
    private fun settings(privacyOptionsRequired: Boolean, onOpenPrivacyOptions: () -> Unit) {
        settingsDialog(
            profileStatus = ProfileProvisioningStatus.available,
            themeMode = ThemeMode.system,
            appLanguage = AppLanguage.system,
            privacyOptionsRequired = privacyOptionsRequired,
            privacyLockEnabled = false,
            privacyLockAvailable = true,
            onThemeModeChange = {},
            onLanguageChange = {},
            onOpenAndroidSettings = {},
            onOpenPrivacyOptions = onOpenPrivacyOptions,
            onPrivacyLockChange = {},
            onShareDiagnosticReport = {},
            onOpenCompatibilityCenter = {},
            onDismiss = {},
        )
    }
}
