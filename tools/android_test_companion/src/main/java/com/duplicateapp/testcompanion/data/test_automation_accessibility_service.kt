package com.duplicateapp.testcompanion.data

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.duplicateapp.testcompanion.domain.TestRunStatus
import com.duplicateapp.testcompanion.domain.TestScenario

class TestAutomationAccessibilityService : AccessibilityService() {
    private lateinit var store: TestRunStore
    private var step: AutomationStep = AutomationStep.Onboarding
    private var activeRunStartedAtMillis: Long? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = TestRunStore(this)
        Log.i(logTag, "accessibility_service_connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        val run = store.load()
        if (run.status != TestRunStatus.Running) return
        if (run.startedAtMillis != activeRunStartedAtMillis) {
            activeRunStartedAtMillis = run.startedAtMillis
            step = AutomationStep.Onboarding
            Log.i(logTag, "automation_run_loaded started_at=$activeRunStartedAtMillis")
        }
        if (run.startedAtMillis != null && System.currentTimeMillis() - run.startedAtMillis > TestScenario.timeoutMillis) {
            store.fail("Scenario timed out at ${step.name}")
            return
        }

        try {
            process(root)
        } catch (error: Throwable) {
            Log.e(logTag, "automation_event_failed step=${step.name}", error)
            store.fail(error.message ?: error.javaClass.simpleName)
        }
    }

    override fun onInterrupt() {
        Log.w(logTag, "accessibility_service_interrupted")
    }

    private fun process(root: AccessibilityNodeInfo) {
        when (step) {
            AutomationStep.Onboarding -> {
                if (clickFirst(root, TestScenario.continueLabels) || containsAny(root, TestScenario.workspaceMarkers)) {
                    moveTo(AutomationStep.OpenSettings, "Opening Settings")
                }
            }
            AutomationStep.OpenSettings -> if (clickFirst(root, TestScenario.settingsLabels)) {
                moveTo(AutomationStep.VerifySettings, "Verifying Settings")
            }
            AutomationStep.VerifySettings -> if (containsAny(root, TestScenario.settingsMarkers)) {
                clickFirst(root, TestScenario.closeLabels)
                moveTo(AutomationStep.OpenAbout, "Opening About")
            }
            AutomationStep.OpenAbout -> if (clickFirst(root, TestScenario.aboutLabels)) {
                moveTo(AutomationStep.VerifyAbout, "Verifying About")
            }
            AutomationStep.VerifyAbout -> if (containsAny(root, TestScenario.aboutMarkers)) {
                clickFirst(root, TestScenario.gotItLabels)
                step = AutomationStep.Done
                store.pass()
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            AutomationStep.Done -> Unit
        }
    }

    private fun moveTo(next: AutomationStep, label: String) {
        Log.i(logTag, "automation_step from=${step.name} to=${next.name}")
        step = next
        store.updateStep(label)
    }

    private fun containsAny(root: AccessibilityNodeInfo, labels: List<String>): Boolean =
        labels.any { label -> root.findAccessibilityNodeInfosByText(label).isNotEmpty() }

    private fun clickFirst(root: AccessibilityNodeInfo, labels: List<String>): Boolean {
        for (label in labels) {
            val node = root.findAccessibilityNodeInfosByText(label).firstOrNull() ?: findByDescription(root, label)
            if (node != null && clickNodeOrParent(node)) return true
        }
        return false
    }

    private fun findByDescription(node: AccessibilityNodeInfo, label: String): AccessibilityNodeInfo? {
        if (node.contentDescription?.toString() == label) return node
        for (index in 0 until node.childCount) {
            val result = node.getChild(index)?.let { child -> findByDescription(child, label) }
            if (result != null) return result
        }
        return null
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        var clickable: AccessibilityNodeInfo? = node
        while (clickable != null && !clickable.isClickable) clickable = clickable.parent
        return clickable?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
    }

    private enum class AutomationStep { Onboarding, OpenSettings, VerifySettings, OpenAbout, VerifyAbout, Done }

    companion object {
        private const val logTag = "TestAutomation"
    }
}
