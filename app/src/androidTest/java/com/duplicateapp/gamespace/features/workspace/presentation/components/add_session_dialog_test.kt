package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.features.workspace.domain.InstalledGame
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget
import org.junit.Assert.assertEquals
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
                addSessionDialog(games = emptyList(), onDismiss = {}, onProfileChange = {}, onAdd = { _, _ -> })
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.no_apps_in_profile)).assertIsDisplayed()
    }

    @Test
    fun selectingGameReturnsItsDefaultDisplayName() {
        val game = InstalledGame(gameName, packageName, ProfileTarget.personal)
        var selectedName: String? = null
        var selectedGame: InstalledGame? = null

        composeRule.setContent {
            MaterialTheme {
                addSessionDialog(
                    games = listOf(game),
                    onDismiss = {},
                    onProfileChange = {},
                    onAdd = { name, installedGame ->
                        selectedName = name
                        selectedGame = installedGame
                    },
                )
            }
        }

        composeRule.onNodeWithText(gameName).performClick()

        assertEquals(gameName, selectedName)
        assertEquals(game, selectedGame)
    }

    @Test
    fun selectingWorkProfileRequestsItsCatalog() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var selectedProfile: ProfileTarget? = null

        composeRule.setContent {
            MaterialTheme {
                addSessionDialog(
                    games = emptyList(),
                    onDismiss = {},
                    onProfileChange = { profile -> selectedProfile = profile },
                    onAdd = { _, _ -> },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.work_profile)).performClick()

        assertEquals(ProfileTarget.managed, selectedProfile)
    }

    private companion object {
        const val gameName = "Test Game"
        const val packageName = "com.example.testgame"
    }
}
