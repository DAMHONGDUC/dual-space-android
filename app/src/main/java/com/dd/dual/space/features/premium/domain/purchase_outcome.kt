package com.dd.dual.space.features.premium.domain

sealed interface PurchaseOutcome {
    data class Activated(val access: PremiumAccess) : PurchaseOutcome
    data class NotActivated(val access: PremiumAccess) : PurchaseOutcome
    data object Pending : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data object Failed : PurchaseOutcome
}

// A completed store transaction is only a success when the entitlement is active.
fun purchaseOutcomeFor(access: PremiumAccess): PurchaseOutcome =
    if (access.status == PremiumStatus.active) PurchaseOutcome.Activated(access) else PurchaseOutcome.NotActivated(access)
