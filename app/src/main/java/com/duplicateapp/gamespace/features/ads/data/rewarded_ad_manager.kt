package com.duplicateapp.gamespace.features.ads.data

import android.app.Activity
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.UUID

class RewardedAdManager(private val adUnitId: String) {
    fun show(activity: Activity, onReward: (String) -> Unit, onUnavailable: () -> Unit) {
        AppLogger.action("show_rewarded_ad", emptyMap())
        if (adUnitId.isBlank()) {
            AppLogger.error("show_rewarded_ad", IllegalStateException("Rewarded ad unit is not configured"), emptyMap())
            onUnavailable()
            return
        }
        MobileAds.initialize(activity) {
            RewardedAd.load(activity, adUnitId, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    var rewardEarned = false
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            if (!rewardEarned) onUnavailable()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            AppLogger.error("show_rewarded_ad", IllegalStateException(error.message), mapOf("code" to error.code))
                            onUnavailable()
                        }
                    }
                    ad.show(activity) {
                        rewardEarned = true
                        AppLogger.success("show_rewarded_ad", emptyMap())
                        onReward(UUID.randomUUID().toString())
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    AppLogger.error("show_rewarded_ad", IllegalStateException(error.message), mapOf("code" to error.code))
                    onUnavailable()
                }
            })
        }
    }
}
