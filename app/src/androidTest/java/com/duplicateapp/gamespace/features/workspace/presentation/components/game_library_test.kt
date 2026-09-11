package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioningStatus
import com.duplicateapp.gamespace.features.workspace.domain.AccountColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameLibraryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyLibraryShowsPrimaryAddAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var addRequested = false

        composeRule.setContent {
            MaterialTheme {
                gameLibrary(
                    sessions = emptyList(),
                    profileStatus = ProfileProvisioningStatus.alreadyCreated,
                    contentPadding = PaddingValues(),
                    onLaunch = {},
                    onAdd = { addRequested = true },
                    onDeleteGame = {},
                    onCreateProfile = {},
                    onOpenAndroidSettings = {},
                    onSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.empty_library_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.add_game)).performClick()

        assertTrue(addRequested)
    }

    @Test
    fun gameRowShowsAndLaunchesBothAccounts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val launchedSessionIds = mutableListOf<String>()
        val sessions: List<GameSession> = listOf(
            session(id = "personal", profileTarget = ProfileTarget.personal),
            session(id = "managed", profileTarget = ProfileTarget.managed),
        )

        composeRule.setContent {
            MaterialTheme {
                gameLibrary(
                    sessions = sessions,
                    profileStatus = ProfileProvisioningStatus.alreadyCreated,
                    contentPadding = PaddingValues(),
                    onLaunch = launchedSessionIds::add,
                    onAdd = {},
                    onDeleteGame = {},
                    onCreateProfile = {},
                    onOpenAndroidSettings = {},
                    onSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(gameName).assertIsDisplayed()
        composeRule.onNodeWithText("personal").performClick()
        composeRule.onNodeWithText("managed").performClick()

        assertEquals(listOf("personal", "managed"), launchedSessionIds)
    }

    private fun session(id: String, profileTarget: ProfileTarget): GameSession = GameSession(
        id = id,
        name = id,
        gameName = gameName,
        accountColor = AccountColor.blue,
        lastOpenedAtEpochMillis = null,
        packageName = packageName,
        profileTarget = profileTarget,
    )

    private companion object {
        const val gameName = "Test Game"
        const val packageName = "com.example.testgame"
    }
}
