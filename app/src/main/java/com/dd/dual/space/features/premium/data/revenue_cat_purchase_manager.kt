package com.dd.dual.space.features.premium.data

import android.app.Activity
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.premium.domain.PremiumAccess
import com.dd.dual.space.features.premium.domain.PremiumStatus
import com.dd.dual.space.features.premium.domain.PurchaseOutcome
import com.dd.dual.space.features.premium.domain.purchaseOutcomeFor
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction

class RevenueCatPurchaseManager(private val entitlementId: String) {
    fun purchase(activity: Activity, onOutcome: (PurchaseOutcome) -> Unit) {
        AppLogger.action("purchase_premium", emptyMap())
        if (!Purchases.isConfigured) {
            AppLogger.error("purchase_premium", IllegalStateException("RevenueCat is not configured"), emptyMap())
            onOutcome(PurchaseOutcome.Failed)
            return
        }
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                val purchasePackage = offerings.current?.availablePackages?.firstOrNull()
                if (purchasePackage == null) {
                    AppLogger.error("purchase_premium", IllegalStateException("No RevenueCat package is available"), emptyMap())
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
}
