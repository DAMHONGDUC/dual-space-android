package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.ParallelAppDimensions
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioningStatus
import com.duplicateapp.gamespace.features.settings.domain.ThemeMode
import com.duplicateapp.gamespace.features.settings.domain.AppLanguage
import com.duplicateapp.gamespace.features.auth.domain.AuthSession
import com.duplicateapp.gamespace.features.premium.domain.PremiumAccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settingsDialog(
    remainingQuotaHours: Int,
    profileStatus: ProfileProvisioningStatus,
    themeMode: ThemeMode,
    appLanguage: AppLanguage,
    authSession: AuthSession?,
    premiumAccess: PremiumAccess,
    isMonetizationBusy: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenAndroidSettings: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onPurchasePremium: () -> Unit,
    onRestorePremium: () -> Unit,
    onWatchRewardedAd: () -> Unit,
    onDismiss: () -> Unit,
) {
    val profileLabel: String = stringResource(
        when (profileStatus) {
            ProfileProvisioningStatus.available -> R.string.profile_available
            ProfileProvisioningStatus.alreadyCreated -> R.string.profile_count
            ProfileProvisioningStatus.unsupported -> R.string.profile_unsupported
        },
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8)) {
                Text(pluralStringResource(R.plurals.settings_quota, remainingQuotaHours, remainingQuotaHours))
                Text(stringResource(R.string.settings_profile, profileLabel))
                Text(
                    stringResource(if (premiumAccess.hasUnlimitedPlayTime) R.string.premium_active else R.string.premium_free),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                )
                authSession?.let { session ->
                    Text(stringResource(R.string.signed_in_as, session.email ?: session.displayName ?: session.userId))
                }
                if (authSession == null) {
                    TextButton(onClick = onSignIn, enabled = !isMonetizationBusy) { Text(stringResource(R.string.sign_in_google)) }
                } else {
                    TextButton(onClick = onSignOut, enabled = !isMonetizationBusy) { Text(stringResource(R.string.sign_out)) }
                }
                if (!premiumAccess.hasUnlimitedPlayTime) {
                    TextButton(onClick = onPurchasePremium, enabled = !isMonetizationBusy) { Text(stringResource(R.string.upgrade_premium)) }
                    TextButton(onClick = onWatchRewardedAd, enabled = !isMonetizationBusy) { Text(stringResource(R.string.watch_ad_for_hour)) }
                }
                TextButton(onClick = onRestorePremium, enabled = !isMonetizationBusy) { Text(stringResource(R.string.restore_purchases)) }
                Text(stringResource(R.string.theme), style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                            label = { Text(themeModeLabel(mode)) },
                        )
                    }
                }
                languageSelector(appLanguage, onLanguageChange)
                Text(stringResource(R.string.settings_privacy))
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenAndroidSettings) { Text(stringResource(R.string.android_settings)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
}

@Composable
private fun themeModeLabel(themeMode: ThemeMode): String = stringResource(
    when (themeMode) {
        ThemeMode.system -> R.string.theme_system
        ThemeMode.light -> R.string.theme_light
        ThemeMode.dark -> R.string.theme_dark
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun languageSelector(selectedLanguage: AppLanguage, onLanguageChange: (AppLanguage) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space4)) {
        Text(stringResource(R.string.language), style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = !isExpanded }) {
            OutlinedTextField(
                value = appLanguageLabel(selectedLanguage),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = androidx.compose.ui.Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                AppLanguage.entries.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(appLanguageLabel(language)) },
                        onClick = {
                            isExpanded = false
                            onLanguageChange(language)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun appLanguageLabel(language: AppLanguage): String = stringResource(
    when (language) {
        AppLanguage.system -> R.string.language_system
        AppLanguage.english -> R.string.language_english
        AppLanguage.vietnamese -> R.string.language_vietnamese
        AppLanguage.spanish -> R.string.language_spanish
        AppLanguage.portugueseBrazil -> R.string.language_portuguese_brazil
        AppLanguage.french -> R.string.language_french
        AppLanguage.german -> R.string.language_german
        AppLanguage.indonesian -> R.string.language_indonesian
        AppLanguage.hindi -> R.string.language_hindi
        AppLanguage.japanese -> R.string.language_japanese
        AppLanguage.korean -> R.string.language_korean
        AppLanguage.chineseSimplified -> R.string.language_chinese_simplified
    },
)

@Composable
fun deleteSessionDialog(sessionName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_session)) },
        text = { Text(stringResource(R.string.delete_session_message, sessionName)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
fun deleteGameDialog(gameName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_game)) },
        text = { Text(stringResource(R.string.delete_game_message, gameName)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
