package com.duplicateapp.testcompanion.presentation

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duplicateapp.testcompanion.data.TestRunStore
import com.duplicateapp.testcompanion.domain.TestRun
import com.duplicateapp.testcompanion.domain.TestRunStatus
import com.duplicateapp.testcompanion.domain.TestScenario
import java.text.DateFormat
import java.util.Date
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TestCompanionScreen(
                    loadRun = { TestRunStore(this).load() },
                    accessibilityEnabled = { isAccessibilityEnabled() },
                    onStart = { startTest() },
                )
            }
        }
    }

    private fun startTest() {
        if (!isAccessibilityEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }
        val launchIntent = packageManager.getLaunchIntentForPackage(TestScenario.targetPackage)
        if (launchIntent == null) {
            TestRunStore(this).fail("Parallel Game Space is not installed")
            return
        }
        TestRunStore(this).start()
        startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun isAccessibilityEnabled(): Boolean {
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { service -> service.resolveInfo.serviceInfo.packageName == packageName }
    }
}

@Composable
private fun TestCompanionScreen(
    loadRun: () -> TestRun,
    accessibilityEnabled: () -> Boolean,
    onStart: () -> Unit,
) {
    var run by remember { mutableStateOf(loadRun()) }
    var enabled by remember { mutableStateOf(accessibilityEnabled()) }
    DisposableEffect(Unit) {
        val timer = Timer("status-refresh", true)
        timer.scheduleAtFixedRate(0L, 500L) {
            run = loadRun()
            enabled = accessibilityEnabled()
        }
        onDispose { timer.cancel() }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Android Test Companion", style = MaterialTheme.typography.headlineMedium)
            Text("Parallel Game Space · safe UI regression flow")
            Text(if (enabled) "Accessibility: ready" else "Accessibility: setup required")
            Text("Status: ${run.status.name}\nStep: ${run.step}")
            run.finishedAtMillis?.let { timestamp ->
                Text("Finished: ${DateFormat.getDateTimeInstance().format(Date(timestamp))}")
            }
            run.error?.let { error -> Text(error, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onStart, enabled = run.status != TestRunStatus.Running) {
                Text(if (enabled) "START AUTOMATED TEST" else "ENABLE ACCESSIBILITY")
            }
            Text("The tool never reads passwords, signs in to Google, changes IP, or joins Play testing.")
        }
    }
}
