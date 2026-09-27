package com.dd.dual.space.features.premium.domain

enum class PremiumPeriod {
    annual,
    monthly,
    lifetime,
    other,
}

/** The exact package the upgrade button shows; purchase must buy this identifier and nothing else. */
data class PremiumOffer(
    val packageIdentifier: String,
    val period: PremiumPeriod,
    val formattedPrice: String,
)

data class OfferCandidate(val packageIdentifier: String, val period: PremiumPeriod)

/**
 * Picks the offer deterministically instead of relying on the remote package order. Annual first
 * because it is the plan most subscribers keep; the owner changes which plans exist in RevenueCat.
 */
fun selectOffer(candidates: List<OfferCandidate>): OfferCandidate? =
    candidates.minByOrNull { candidate -> candidate.period.ordinal }
