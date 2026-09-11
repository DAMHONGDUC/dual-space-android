package com.duplicateapp.gamespace.features.quota.domain

import com.duplicateapp.gamespace.features.premium.domain.PremiumAccess
import com.duplicateapp.gamespace.features.premium.domain.PremiumStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayAccessPolicyTest {
    @Test
    fun `free user cannot launch after base quota is exhausted`() {
        val access = PremiumAccess(PremiumStatus.inactive)

        assertFalse(PlayAccessPolicy.canLaunch(FreeQuotaPolicy.monthlyLimitSeconds, 0L, access))
    }

    @Test
    fun `reward grants one additional hour`() {
        val remainingSeconds: Long = PlayAccessPolicy.remainingSeconds(
            usedSeconds = FreeQuotaPolicy.monthlyLimitSeconds,
            rewardedBonusSeconds = PlayAccessPolicy.rewardedSeconds,
        )

        assertEquals(FreeQuotaPolicy.secondsPerHour, remainingSeconds)
    }

    @Test
    fun `active premium can launch with exhausted quota`() {
        val access = PremiumAccess(PremiumStatus.active, userId = "firebase-user")

        assertTrue(PlayAccessPolicy.canLaunch(Long.MAX_VALUE, 0L, access))
        assertTrue(access.removesAds)
    }

    @Test
    fun `unknown entitlement fails closed`() {
        val access = PremiumAccess(PremiumStatus.unknown)

        assertFalse(PlayAccessPolicy.canLaunch(FreeQuotaPolicy.monthlyLimitSeconds, 0L, access))
        assertFalse(access.removesAds)
    }

    @Test
    fun `negative balances are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            PlayAccessPolicy.remainingSeconds(usedSeconds = -1L, rewardedBonusSeconds = 0L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PlayAccessPolicy.remainingSeconds(usedSeconds = 0L, rewardedBonusSeconds = -1L)
        }
    }
}
