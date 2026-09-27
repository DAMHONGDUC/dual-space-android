package com.dd.dual.space.features.workspace.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dd.dual.space.R
import com.dd.dual.space.features.workspace.domain.InstalledGame
import com.dd.dual.space.features.workspace.domain.ProfileTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddSessionDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyCatalogExplainsThatNoAppsAreAvailable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MaterialTheme {
                addSessionDialog(games = emptyList(), onDismiss = {}, onAdd = { _, _ -> }, onRefresh = {}, onOpenSettings = {})
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.no_apps_in_profile)).assertIsDisplayed()
    }

    @Test
    fun selectingGameReturnsItsDefaultDisplayName() {
        val game = InstalledGame(gameName, packageName, ProfileTarget.managed)
        var selectedName: String? = null
        var selectedGame: InstalledGame? = null

        composeRule.setContent {
            MaterialTheme {
                addSessionDialog(
                    games = listOf(game),
                    onDismiss = {},
                    onAdd = { name, installedGame ->
                        selectedName = name
                        selectedGame = installedGame
                    },
                    onRefresh = {},
                    onOpenSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(gameName).performClick()

        assertEquals(gameName, selectedName)
        assertEquals(game, selectedGame)
    }

    @Test
    fun unavailableCopyCannotBeAdded() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val game = InstalledGame(gameName, packageName, ProfileTarget.personal, isCopyAvailable = false)
        var refreshRequested = false
        var settingsRequested = false

        composeRule.setContent {
            MaterialTheme {
                addSessionDialog(
                    games = listOf(game),
                    onDismiss = {},
                    onAdd = { _, _ -> },
                    onRefresh = { refreshRequested = true },
                    onOpenSettings = { settingsRequested = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.copy_install_required)).assertIsDisplayed()
        composeRule.onNodeWithText(gameName).assertIsNotEnabled()
        composeRule.onNodeWithText(context.getString(R.string.rescan)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.android_settings)).performClick()
        assertTrue(refreshRequested)
        assertTrue(settingsRequested)
    }

    private companion object {
        const val gameName = "Test Game"
        const val packageName = "com.example.testgame"
    }
}
