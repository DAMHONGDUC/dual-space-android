package com.dd.dual.space.features.premium.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumAccessTest {
    @Test
    fun `free access shows ads`() {
        val access = PremiumAccess(PremiumStatus.inactive)

        assertTrue(access.showsAds)
        assertFalse(access.removesAds)
    }

    @Test
    fun `premium access removes ads`() {
        val access = PremiumAccess(PremiumStatus.active)

        assertFalse(access.showsAds)
        assertTrue(access.removesAds)
    }

    @Test
    fun `unknown access does not flash ads while entitlement loads`() {
        val access = PremiumAccess(PremiumStatus.unknown)

        assertFalse(access.showsAds)
    }
}
