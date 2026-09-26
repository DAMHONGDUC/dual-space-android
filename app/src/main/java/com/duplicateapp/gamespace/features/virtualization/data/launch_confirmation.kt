package com.duplicateapp.gamespace.features.virtualization.data

internal class LaunchConfirmation(
    private val nowMillis: () -> Long,
    private val pause: (Long) -> Unit,
) {
    fun await(timeoutMillis: Long, isConfirmed: () -> Boolean): Boolean {
        val startedAt: Long = nowMillis()
        while (!Thread.currentThread().isInterrupted) {
            if (isConfirmed()) return true
            val remaining: Long = timeoutMillis - (nowMillis() - startedAt)
            if (remaining <= 0L) return false
            pause(minOf(pollIntervalMillis, remaining))
        }
        return false
    }

    private companion object {
        const val pollIntervalMillis: Long = 100L
    }
}
