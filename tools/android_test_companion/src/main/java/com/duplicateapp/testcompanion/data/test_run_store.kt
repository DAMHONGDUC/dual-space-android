package com.duplicateapp.testcompanion.data

import android.content.Context
import android.util.Log
import com.duplicateapp.testcompanion.domain.TestRun
import com.duplicateapp.testcompanion.domain.TestRunStatus

class TestRunStore(context: Context) {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    fun load(): TestRun = TestRun(
        status = TestRunStatus.valueOf(preferences.getString(statusKey, TestRunStatus.Idle.name)!!),
        step = preferences.getString(stepKey, "Ready")!!,
        startedAtMillis = preferences.getLong(startedAtKey, missingTimestamp).takeUnless { it == missingTimestamp },
        finishedAtMillis = preferences.getLong(finishedAtKey, missingTimestamp).takeUnless { it == missingTimestamp },
        error = preferences.getString(errorKey, null),
    )

    fun start() {
        Log.i(logTag, "test_run_start")
        save(TestRun(TestRunStatus.Running, "Opening target app", System.currentTimeMillis(), null, null))
    }

    fun updateStep(step: String) {
        val current = load()
        Log.i(logTag, "test_run_step step=$step")
        save(current.copy(step = step))
    }

    fun pass() {
        val current = load()
        Log.i(logTag, "test_run_pass")
        save(current.copy(status = TestRunStatus.Passed, step = "Completed", finishedAtMillis = System.currentTimeMillis()))
    }

    fun fail(error: String) {
        val current = load()
        Log.e(logTag, "test_run_fail error=$error")
        save(current.copy(status = TestRunStatus.Failed, step = "Failed", finishedAtMillis = System.currentTimeMillis(), error = error))
    }

    private fun save(run: TestRun) {
        preferences.edit()
            .putString(statusKey, run.status.name)
            .putString(stepKey, run.step)
            .putLong(startedAtKey, run.startedAtMillis ?: missingTimestamp)
            .putLong(finishedAtKey, run.finishedAtMillis ?: missingTimestamp)
            .putString(errorKey, run.error)
            .apply()
    }

    companion object {
        private const val logTag = "TestRunStore"
        private const val preferencesName = "test_run"
        private const val statusKey = "status"
        private const val stepKey = "step"
        private const val startedAtKey = "started_at"
        private const val finishedAtKey = "finished_at"
        private const val errorKey = "error"
        private const val missingTimestamp = -1L
    }
}
