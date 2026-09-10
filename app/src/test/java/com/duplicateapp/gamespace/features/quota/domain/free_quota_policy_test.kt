package com.duplicateapp.gamespace.features.quota.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FreeQuotaPolicyTest {
    @Test
    fun `two running sessions consume two session hours`() {
        val consumedSeconds: Long = FreeQuotaPolicy.consumedSeconds(
            runningSessionCount = 2,
            elapsedSeconds = FreeQuotaPolicy.secondsPerHour,
        )

        assertEquals(2 * FreeQuotaPolicy.secondsPerHour, consumedSeconds)
    }

    @Test
    fun `remaining quota never becomes negative`() {
        val usedSeconds: Long = FreeQuotaPolicy.monthlyLimitSeconds + FreeQuotaPolicy.secondsPerHour

        assertEquals(0L, FreeQuotaPolicy.remainingSeconds(usedSeconds))
    }
}
