package com.duplicateapp.testcompanion.domain

enum class TestRunStatus {
    Idle,
    Running,
    Passed,
    Failed,
}

data class TestRun(
    val status: TestRunStatus,
    val step: String,
    val startedAtMillis: Long?,
    val finishedAtMillis: Long?,
    val error: String?,
)
