package com.dd.dual.space.features.premium.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PurchaseOutcomeTest {
    @Test
    fun `completed purchase with active entitlement is activated`() {
        val access = PremiumAccess(PremiumStatus.active, "user")

        assertEquals(PurchaseOutcome.Activated(access), purchaseOutcomeFor(access))
    }

    @Test
    fun `completed purchase without entitlement is not reported as activated`() {
        val access = PremiumAccess(PremiumStatus.inactive, "user")

        assertEquals(PurchaseOutcome.NotActivated(access), purchaseOutcomeFor(access))
    }

    @Test
    fun `completed purchase with unknown entitlement is not reported as activated`() {
        val access = PremiumAccess(PremiumStatus.unknown)

        assertEquals(PurchaseOutcome.NotActivated(access), purchaseOutcomeFor(access))
    }
}
