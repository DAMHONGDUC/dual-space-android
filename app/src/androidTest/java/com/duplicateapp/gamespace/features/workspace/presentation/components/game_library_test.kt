package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
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
                    onHelp = {},
                    onSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.empty_library_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.add_game)).performClick()

        assertTrue(addRequested)
    }

    @Test
    fun gameRowShowsCopyTagsAndLaunchesBothCopies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val launchedSessionIds = mutableListOf<String>()
        val sessions: List<GameSession> = listOf(
            session(id = "copy-1", virtualUserId = 1),
            session(id = "copy-2", virtualUserId = 2),
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
                    onHelp = {},
                    onSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(gameName).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.copy_number, 1)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.copy_number, 2)).assertIsDisplayed()
        composeRule.onNodeWithText("copy-1").performClick()
        composeRule.onNodeWithText("copy-2").performClick()

        assertEquals(listOf("copy-1", "copy-2"), launchedSessionIds)
    }

    @Test
    fun runningAccountShowsSemanticStatus() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            MaterialTheme {
                gameLibrary(
                    sessions = listOf(session(id = "farm-main", virtualUserId = 1)),
                    profileStatus = ProfileProvisioningStatus.alreadyCreated,
                    runningSessionIds = setOf("farm-main"),
                    contentPadding = PaddingValues(),
                    onLaunch = {},
                    onAdd = {},
                    onDeleteGame = {},
                    onCreateProfile = {},
                    onOpenAndroidSettings = {},
                    onHelp = {},
                    onSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.session_running)).assertIsDisplayed()
    }

    @Test
    fun accountAreaOccupiesTwoFifthsOfParentRow() {
        composeRule.setContent {
            MaterialTheme {
                gameLibrary(
                    sessions = listOf(session(id = "copy-1", virtualUserId = 1)),
                    profileStatus = ProfileProvisioningStatus.alreadyCreated,
                    contentPadding = PaddingValues(),
                    onLaunch = {},
                    onAdd = {},
                    onDeleteGame = {},
                    onCreateProfile = {},
                    onOpenAndroidSettings = {},
                    onHelp = {},
                    onSettings = {},
                )
            }
        }

        val parentWidth: Float = composeRule.onNodeWithTag(gameRowContentTestTag)
            .fetchSemanticsNode().boundsInRoot.width
        val accountAreaWidth: Float = composeRule.onNodeWithTag(accountAreaTestTag)
            .fetchSemanticsNode().boundsInRoot.width

        assertEquals(parentWidth * accountAreaWidthFraction, accountAreaWidth, widthTolerancePixels)
    }

    private fun session(id: String, virtualUserId: Int): GameSession = GameSession(
        id = id,
        name = id,
        gameName = gameName,
        accountColor = AccountColor.blue,
        lastOpenedAtEpochMillis = null,
        packageName = packageName,
        profileTarget = ProfileTarget.managed,
        virtualUserId = virtualUserId,
    )

    private companion object {
        const val gameName = "Test Game"
        const val packageName = "com.example.testgame"
        const val widthTolerancePixels = 1f
    }
}
