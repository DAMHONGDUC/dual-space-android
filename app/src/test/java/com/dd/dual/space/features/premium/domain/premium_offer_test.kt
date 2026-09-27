package com.dd.dual.space.features.premium.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PremiumOfferTest {
    @Test
    fun `selection does not depend on remote package order`() {
        val monthly = OfferCandidate("monthly", PremiumPeriod.monthly)
        val annual = OfferCandidate("annual", PremiumPeriod.annual)

        assertEquals(annual, selectOffer(listOf(monthly, annual)))
        assertEquals(annual, selectOffer(listOf(annual, monthly)))
    }

    @Test
    fun `falls back through monthly and lifetime`() {
        val lifetime = OfferCandidate("lifetime", PremiumPeriod.lifetime)
        val custom = OfferCandidate("custom", PremiumPeriod.other)

        assertEquals(OfferCandidate("monthly", PremiumPeriod.monthly), selectOffer(listOf(custom, lifetime, OfferCandidate("monthly", PremiumPeriod.monthly))))
        assertEquals(lifetime, selectOffer(listOf(custom, lifetime)))
    }

    @Test
    fun `empty offering has no offer`() {
        assertNull(selectOffer(emptyList()))
    }
}
