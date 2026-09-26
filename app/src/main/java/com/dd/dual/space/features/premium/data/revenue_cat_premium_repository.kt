package com.dd.dual.space.features.premium.data

import android.content.Context
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.premium.domain.PremiumAccess
import com.dd.dual.space.features.premium.domain.PremiumRepository
import com.dd.dual.space.features.premium.domain.PremiumStatus
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class RevenueCatPremiumRepository(
    context: Context,
    apiKey: String,
    private val entitlementId: String,
) : PremiumRepository {
    private val isConfigured: Boolean = apiKey.isNotBlank()

    init {
        if (isConfigured && !Purchases.isConfigured) {
            Purchases.configure(PurchasesConfiguration.Builder(context.applicationContext, apiKey).build())
            AppLogger.success("configure_revenue_cat", emptyMap())
        }
    }

    override suspend fun refresh(): PremiumAccess {
        AppLogger.action("refresh_premium", emptyMap())
        return receiveCustomerInfo("refresh_premium") { callback -> Purchases.sharedInstance.getCustomerInfo(callback) }
    }

    override suspend fun identify(userId: String): PremiumAccess {
        require(userId.isNotBlank())
        AppLogger.action("identify_premium_user", mapOf("userIdLength" to userId.length))
        if (!isConfigured) return unavailable("identify_premium_user")
        return suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.logIn(userId, object : LogInCallback {
                override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                    val access: PremiumAccess = customerInfo.toPremiumAccess(userId)
                    AppLogger.success("identify_premium_user", mapOf("created" to created, "premium" to access.removesAds))
                    if (continuation.isActive) continuation.resume(access)
                }

                override fun onError(error: PurchasesError) {
                    AppLogger.error("identify_premium_user", IllegalStateException(error.message), emptyMap())
                    if (continuation.isActive) continuation.resume(PremiumAccess(PremiumStatus.unknown, userId))
                }
            })
        }
    }

    override suspend fun logOut(): PremiumAccess {
        AppLogger.action("logout_premium_user", emptyMap())
        return receiveCustomerInfo("logout_premium_user") { callback -> Purchases.sharedInstance.logOut(callback) }
    }

    override suspend fun restore(): PremiumAccess {
        AppLogger.action("restore_premium", emptyMap())
        return receiveCustomerInfo("restore_premium") { callback -> Purchases.sharedInstance.restorePurchases(callback) }
    }

    private suspend fun receiveCustomerInfo(
        action: String,
        request: (ReceiveCustomerInfoCallback) -> Unit,
    ): PremiumAccess {
        if (!isConfigured) return unavailable(action)
        return suspendCancellableCoroutine { continuation ->
            request(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    val access: PremiumAccess = customerInfo.toPremiumAccess(Purchases.sharedInstance.appUserID)
                    AppLogger.success(action, mapOf("premium" to access.removesAds))
                    if (continuation.isActive) continuation.resume(access)
                }

                override fun onError(error: PurchasesError) {
                    AppLogger.error(action, IllegalStateException(error.message), emptyMap())
                    if (continuation.isActive) continuation.resume(PremiumAccess(PremiumStatus.unknown))
                }
            })
        }
    }

    private fun unavailable(action: String): PremiumAccess {
        AppLogger.error(action, IllegalStateException("RevenueCat is not configured"), emptyMap())
        return PremiumAccess(PremiumStatus.unknown)
    }

    private fun CustomerInfo.toPremiumAccess(userId: String?): PremiumAccess = PremiumAccess(
        status = if (entitlements.active.containsKey(entitlementId)) PremiumStatus.active else PremiumStatus.inactive,
        userId = userId,
    )
}
