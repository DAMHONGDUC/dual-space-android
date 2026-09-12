package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.features.workspace.domain.AccountColor
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountIdentityDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun savesUpdatedNameAndColor() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var savedName: String? = null
        var savedColor: AccountColor? = null
        composeRule.setContent {
            MaterialTheme {
                accountIdentityDialog(
                    session = session,
                    onSave = { name, color -> savedName = name; savedColor = color },
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText(session.name).performTextReplacement("Gaming")
        composeRule.onNodeWithText(context.getString(R.string.color_green)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()

        assertEquals("Gaming", savedName)
        assertEquals(AccountColor.green, savedColor)
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
