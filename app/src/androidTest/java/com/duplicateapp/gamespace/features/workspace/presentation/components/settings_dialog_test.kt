package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.features.premium.domain.PremiumAccess
import com.duplicateapp.gamespace.features.premium.domain.PremiumStatus
import com.duplicateapp.gamespace.features.settings.domain.AppLanguage
import com.duplicateapp.gamespace.features.settings.domain.ThemeMode
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioningStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun freePlanOffersSignInAndAdFreeUpgrade() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var signInRequested = false

        composeRule.setContent {
            MaterialTheme {
                settingsDialog(
                    profileStatus = ProfileProvisioningStatus.available,
                    themeMode = ThemeMode.system,
                    appLanguage = AppLanguage.system,
                    authSession = null,
                    premiumAccess = PremiumAccess(PremiumStatus.inactive),
                    isMonetizationBusy = false,
                    onThemeModeChange = {},
                    onLanguageChange = {},
                    onOpenAndroidSettings = {},
                    onSignIn = { signInRequested = true },
                    onSignOut = {},
                    onPurchasePremium = {},
                    onRestorePremium = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.sign_in_google)).performClick()
        assertTrue(signInRequested)
    }

    @Test
    fun premiumPlanDoesNotOfferAdsOrPurchase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MaterialTheme {
                settingsDialog(
                    profileStatus = ProfileProvisioningStatus.available,
                    themeMode = ThemeMode.system,
                    appLanguage = AppLanguage.system,
                    authSession = null,
                    premiumAccess = PremiumAccess(PremiumStatus.active),
                    isMonetizationBusy = false,
                    onThemeModeChange = {},
                    onLanguageChange = {},
                    onOpenAndroidSettings = {},
                    onSignIn = {},
                    onSignOut = {},
                    onPurchasePremium = {},
                    onRestorePremium = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onAllNodesWithText(context.getString(R.string.upgrade_premium)).assertCountEquals(0)
        composeRule.onNodeWithText(context.getString(R.string.premium_active)).assertIsDisplayed()
    }
}
