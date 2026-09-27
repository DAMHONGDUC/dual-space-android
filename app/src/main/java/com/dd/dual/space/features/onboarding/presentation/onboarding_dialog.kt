package com.dd.dual.space.features.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dd.dual.space.R
import com.dd.dual.space.core.theme.ParallelAppDimensions

@Composable
fun onboardingDialog(onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        icon = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(ParallelAppDimensions.itemCornerRadius),
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(ParallelAppDimensions.space12),
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12)) {
                onboardingItem(Icons.Filled.ContentCopy, stringResource(R.string.onboarding_profiles))
                onboardingItem(Icons.Filled.WorkspacePremium, stringResource(R.string.onboarding_monetization))
                onboardingItem(Icons.Filled.Shield, stringResource(R.string.onboarding_privacy))
            }
        },
        confirmButton = {
            Button(onClick = onContinue) { Text(stringResource(R.string.continue_label)) }
        },
        shape = RoundedCornerShape(ParallelAppDimensions.dialogCornerRadius),
    )
}

@Composable
private fun onboardingItem(icon: ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(ParallelAppDimensions.itemCornerRadius),
    ) {
        Row(
            modifier = Modifier.padding(ParallelAppDimensions.space12),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
