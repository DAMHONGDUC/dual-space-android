package com.dd.dual.space.features.privacy.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dd.dual.space.R
import com.dd.dual.space.core.theme.ParallelAppDimensions

@Composable
fun privacyLockScreen(onUnlock: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space16, Alignment.CenterVertically),
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.privacy_lock_title), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.privacy_lock_description), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onUnlock) { Text(stringResource(R.string.unlock)) }
        }
    }
}
