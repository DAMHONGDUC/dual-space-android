package com.dd.dual.space.features.premium.data

import android.app.Activity
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.premium.domain.PremiumAccess
import com.dd.dual.space.features.premium.domain.PremiumStatus
import com.dd.dual.space.features.premium.domain.OfferCandidate
import com.dd.dual.space.features.premium.domain.PremiumOffer
import com.dd.dual.space.features.premium.domain.PremiumPeriod
import com.dd.dual.space.features.premium.domain.PurchaseOutcome
import com.dd.dual.space.features.premium.domain.selectOffer
import com.dd.dual.space.features.premium.domain.purchaseOutcomeFor
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction

class RevenueCatPurchaseManager(private val entitlementId: String) {
    fun loadOffer(onOffer: (PremiumOffer?) -> Unit) {
        AppLogger.action("load_premium_offer", emptyMap())
        if (!Purchases.isConfigured) {
            onOffer(null)
            return
        }
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                val offer: PremiumOffer? = selectPackage(offerings)?.toOffer()
                AppLogger.success("load_premium_offer", mapOf("period" to offer?.period?.name))
                onOffer(offer)
            }

            override fun onError(error: PurchasesError) {
                AppLogger.error("load_premium_offer", IllegalStateException(error.message), mapOf("code" to error.code.name))
                onOffer(null)
            }
        })
    }

    fun purchase(activity: Activity, offer: PremiumOffer, onOutcome: (PurchaseOutcome) -> Unit) {
        AppLogger.action("purchase_premium", mapOf("period" to offer.period.name))
        if (!Purchases.isConfigured) {
            AppLogger.error("purchase_premium", IllegalStateException("RevenueCat is not configured"), emptyMap())
            onOutcome(PurchaseOutcome.Failed)
            return
        }
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                // Buy only the package the user saw; a changed offering must not switch the product.
                val purchasePackage: Package? = offerings.current?.availablePackages
                    ?.firstOrNull { candidate -> candidate.identifier == offer.packageIdentifier }
                if (purchasePackage == null) {
                    AppLogger.error("purchase_premium", IllegalStateException("Displayed package is no longer offered"), emptyMap())
                    onOutcome(PurchaseOutcome.Failed)
                    return
                }
                val purchaseParams: PurchaseParams = PurchaseParams.Builder(activity, purchasePackage).build()
                Purchases.sharedInstance.purchase(purchaseParams, object : PurchaseCallback {
                    override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                        val active: Boolean = customerInfo.entitlements.active.containsKey(entitlementId)
                        AppLogger.success("purchase_premium", mapOf("premium" to active))
                        val access = PremiumAccess(if (active) PremiumStatus.active else PremiumStatus.inactive, Purchases.sharedInstance.appUserID)
                        onOutcome(purchaseOutcomeFor(access))
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        when {
                            userCancelled -> {
                                AppLogger.success("purchase_premium", mapOf("cancelled" to true))
                                onOutcome(PurchaseOutcome.Cancelled)
                            }
                            error.code == PurchasesErrorCode.PaymentPendingError -> {
                                AppLogger.success("purchase_premium", mapOf("pending" to true))
                                onOutcome(PurchaseOutcome.Pending)
                            }
                            else -> {
                                AppLogger.error("purchase_premium", IllegalStateException(error.message), mapOf("code" to error.code.name))
                                onOutcome(PurchaseOutcome.Failed)
                            }
                        }
                    }
                })
            }

            override fun onError(error: PurchasesError) {
                AppLogger.error("purchase_premium", IllegalStateException(error.message), emptyMap())
                onOutcome(PurchaseOutcome.Failed)
            }
        })
    }

    private fun selectPackage(offerings: Offerings): Package? {
        val packages: List<Package> = offerings.current?.availablePackages.orEmpty()
        val chosen: OfferCandidate = selectOffer(packages.map { candidate -> OfferCandidate(candidate.identifier, candidate.packageType.toPeriod()) })
            ?: return null
        return packages.firstOrNull { candidate -> candidate.identifier == chosen.packageIdentifier }
    }

    private fun Package.toOffer(): PremiumOffer = PremiumOffer(identifier, packageType.toPeriod(), product.price.formatted)

    private fun PackageType.toPeriod(): PremiumPeriod = when (this) {
        PackageType.ANNUAL -> PremiumPeriod.annual
        PackageType.MONTHLY -> PremiumPeriod.monthly
        PackageType.LIFETIME -> PremiumPeriod.lifetime
        else -> PremiumPeriod.other
    }
}
