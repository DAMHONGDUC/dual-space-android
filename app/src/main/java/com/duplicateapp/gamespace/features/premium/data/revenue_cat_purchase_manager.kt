package com.duplicateapp.gamespace.features.premium.data

import android.app.Activity
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.premium.domain.PremiumAccess
import com.duplicateapp.gamespace.features.premium.domain.PremiumStatus
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction

class RevenueCatPurchaseManager(private val entitlementId: String) {
    fun purchase(activity: Activity, onResult: (PremiumAccess) -> Unit, onUnavailable: () -> Unit) {
        AppLogger.action("purchase_premium", emptyMap())
        if (!Purchases.isConfigured) {
            AppLogger.error("purchase_premium", IllegalStateException("RevenueCat is not configured"), emptyMap())
            onUnavailable()
            return
        }
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                val purchasePackage = offerings.current?.availablePackages?.firstOrNull()
                if (purchasePackage == null) {
                    AppLogger.error("purchase_premium", IllegalStateException("No RevenueCat package is available"), emptyMap())
                    onUnavailable()
                    return
                }
                val purchaseParams: PurchaseParams = PurchaseParams.Builder(activity, purchasePackage).build()
                Purchases.sharedInstance.purchase(purchaseParams, object : PurchaseCallback {
                    override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                        val active: Boolean = customerInfo.entitlements.active.containsKey(entitlementId)
                        AppLogger.success("purchase_premium", mapOf("premium" to active))
                        onResult(PremiumAccess(if (active) PremiumStatus.active else PremiumStatus.inactive, Purchases.sharedInstance.appUserID))
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        if (!userCancelled) AppLogger.error("purchase_premium", IllegalStateException(error.message), emptyMap())
                        onUnavailable()
                    }
                })
            }

            override fun onError(error: PurchasesError) {
                AppLogger.error("purchase_premium", IllegalStateException(error.message), emptyMap())
                onUnavailable()
            }
        })
    }
}
