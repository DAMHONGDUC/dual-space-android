package com.duplicateapp.gamespace.features.quota.domain

import kotlin.math.max

object FreeQuotaPolicy {
    const val monthlyLimitHours = 180
    const val secondsPerHour = 3_600L
    const val monthlyLimitSeconds = monthlyLimitHours * secondsPerHour

    fun remainingSeconds(usedSeconds: Long): Long {
        return max(0L, monthlyLimitSeconds - usedSeconds)
    }

    fun remainingWholeHours(usedSeconds: Long): Int {
        return (remainingSeconds(usedSeconds) / secondsPerHour).toInt()
    }

    fun consumedSeconds(runningSessionCount: Int, elapsedSeconds: Long): Long {
        require(runningSessionCount >= 0)
        require(elapsedSeconds >= 0)
        return runningSessionCount * elapsedSeconds
    }
}
