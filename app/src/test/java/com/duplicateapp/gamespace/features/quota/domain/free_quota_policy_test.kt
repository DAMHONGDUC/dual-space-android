package com.duplicateapp.gamespace.features.quota.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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

    @Test
    fun `unused quota returns the full monthly allowance`() {
        assertEquals(FreeQuotaPolicy.monthlyLimitSeconds, FreeQuotaPolicy.remainingSeconds(0L))
        assertEquals(FreeQuotaPolicy.monthlyLimitHours, FreeQuotaPolicy.remainingWholeHours(0L))
    }

    @Test
    fun `remaining whole hours rounds down partial hours`() {
        val usedSeconds: Long = FreeQuotaPolicy.secondsPerHour / 2

        assertEquals(179, FreeQuotaPolicy.remainingWholeHours(usedSeconds))
    }

    @Test
    fun `negative running session count is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            FreeQuotaPolicy.consumedSeconds(runningSessionCount = -1, elapsedSeconds = 1L)
        }
    }

    @Test
    fun `negative elapsed time is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            FreeQuotaPolicy.consumedSeconds(runningSessionCount = 1, elapsedSeconds = -1L)
        }
    }
}
