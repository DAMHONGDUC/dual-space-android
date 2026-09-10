package com.duplicateapp.gamespace.features.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.game_space_dimensions

@Composable
fun onboarding_dialog(on_continue: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.onboarding_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(game_space_dimensions.space_8)) {
                Text(stringResource(R.string.onboarding_profiles))
                Text(stringResource(R.string.onboarding_limit))
                Text(stringResource(R.string.onboarding_privacy))
            }
        },
        confirmButton = {
            TextButton(onClick = on_continue) { Text(stringResource(R.string.continue_label)) }
        },
    )
}
