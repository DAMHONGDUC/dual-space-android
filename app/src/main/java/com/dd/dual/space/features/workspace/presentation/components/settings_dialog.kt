package com.dd.dual.space.features.workspace.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.dd.dual.space.R
import com.dd.dual.space.core.theme.ParallelAppDimensions
import com.dd.dual.space.features.workspace.domain.ProfileProvisioningStatus
import com.dd.dual.space.features.settings.domain.ThemeMode
import com.dd.dual.space.features.settings.domain.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settingsDialog(
    profileStatus: ProfileProvisioningStatus,
    themeMode: ThemeMode,
    appLanguage: AppLanguage,
    privacyOptionsRequired: Boolean,
    privacyLockEnabled: Boolean,
    privacyLockAvailable: Boolean,
    confirmBeforeLaunch: Boolean = true,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenAndroidSettings: () -> Unit,
    onOpenPrivacyOptions: () -> Unit,
    onPrivacyLockChange: (Boolean) -> Unit,
    onConfirmBeforeLaunchChange: (Boolean) -> Unit = {},
    onShareDiagnosticReport: () -> Unit,
    onOpenCompatibilityCenter: () -> Unit,
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
        icon = {
            dialogIcon(Icons.Filled.Settings)
        },
        title = { Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12),
            ) {
                settingsSection {
                    Text(stringResource(R.string.settings_profile, profileLabel), style = MaterialTheme.typography.bodyMedium)
                    if (privacyOptionsRequired) {
                        TextButton(
                            onClick = onOpenPrivacyOptions,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(stringResource(R.string.privacy_options)) }
                    }
                }
                settingsSection {
                    Text(stringResource(R.string.theme), style = MaterialTheme.typography.labelLarge)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
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
                }
                settingsSection {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.confirm_before_launch), style = MaterialTheme.typography.labelLarge)
                            Text(
                                stringResource(R.string.confirm_before_launch_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = confirmBeforeLaunch, onCheckedChange = onConfirmBeforeLaunchChange)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.privacy_lock_title), style = MaterialTheme.typography.labelLarge)
                            Text(
                                stringResource(R.string.privacy_lock_setting_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = privacyLockEnabled,
                            onCheckedChange = onPrivacyLockChange,
                            enabled = privacyLockAvailable || privacyLockEnabled,
                        )
                    }
                    Text(
                        stringResource(R.string.settings_privacy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(onClick = onShareDiagnosticReport, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.share_diagnostic_report))
                    }
                    OutlinedButton(onClick = onOpenCompatibilityCenter, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.compatibility_center))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenAndroidSettings) { Text(stringResource(R.string.android_settings)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        shape = RoundedCornerShape(ParallelAppDimensions.dialogCornerRadius),
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
        Text(stringResource(R.string.language), style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = !isExpanded }) {
            OutlinedTextField(
                value = appLanguageLabel(selectedLanguage),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
private fun settingsSection(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(ParallelAppDimensions.itemCornerRadius),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ParallelAppDimensions.space16),
            verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8),
            content = content,
        )
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
    destructiveDialog(
        title = stringResource(R.string.delete_session),
        message = stringResource(R.string.delete_session_message, sessionName),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun deleteGameDialog(gameName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    destructiveDialog(
        title = stringResource(R.string.delete_game),
        message = stringResource(R.string.delete_game_message, gameName),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
private fun destructiveDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { dialogIcon(Icons.Filled.DeleteOutline, isDestructive = true) },
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(message, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        shape = RoundedCornerShape(ParallelAppDimensions.dialogCornerRadius),
    )
}

@Composable
fun dialogIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isDestructive: Boolean = false) {
    val containerColor = if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Surface(color = containerColor, shape = RoundedCornerShape(ParallelAppDimensions.itemCornerRadius)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.padding(ParallelAppDimensions.space12),
        )
    }
}
