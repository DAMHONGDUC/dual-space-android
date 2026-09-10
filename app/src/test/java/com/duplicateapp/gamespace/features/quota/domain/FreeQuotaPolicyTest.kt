package com.duplicateapp.gamespace.features.quota.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FreeQuotaPolicyTest {
    @Test
    fun `two running sessions consume two session hours`() {
        val consumedSeconds: Long = FreeQuotaPolicy.consumedSeconds(
            runningSessionCount = 2,
            elapsedSeconds = FreeQuotaPolicy.SecondsPerHour,
        )

        assertEquals(2 * FreeQuotaPolicy.SecondsPerHour, consumedSeconds)
    }

    @Test
    fun `remaining quota never becomes negative`() {
        val usedSeconds: Long = FreeQuotaPolicy.MonthlyLimitSeconds + FreeQuotaPolicy.SecondsPerHour

        assertEquals(0L, FreeQuotaPolicy.remainingSeconds(usedSeconds))
    }
}
