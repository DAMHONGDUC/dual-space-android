package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.features.workspace.domain.AccountColor
import com.duplicateapp.gamespace.features.workspace.domain.GameLaunchReadiness
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LaunchConfirmationDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun readyAccountRequiresConfirmationBeforeLaunch() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var confirmed: Boolean = false
        composeRule.setContent {
            MaterialTheme {
                launchConfirmationDialog(
                    session = session,
                    readiness = GameLaunchReadiness.Ready,
                    onConfirm = { confirmed = true },
                    onDismiss = {},
                    onOpenSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.open_game)).performClick()

        assertTrue(confirmed)
    }

    private val session = GameSession(
        id = "session",
        name = "Work",
        gameName = "Test Game",
        accountColor = AccountColor.blue,
        lastOpenedAtEpochMillis = null,
        packageName = "com.example.game",
        profileTarget = ProfileTarget.managed,
    )
}
