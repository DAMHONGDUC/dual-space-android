package com.duplicateapp.gamespace.features.ads.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

@Composable
fun bannerAd(adUnitId: String, modifier: Modifier = Modifier) {
    if (adUnitId.isBlank()) return

    val context = LocalContext.current
    val adView: AdView = remember(context, adUnitId) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    AppLogger.success("load_banner_ad", emptyMap())
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    AppLogger.error(
                        "load_banner_ad",
                        IllegalStateException(error.message),
                        mapOf("code" to error.code),
                    )
                }
            }
        }
    }

    LaunchedEffect(adView) {
        AppLogger.action("load_banner_ad", emptyMap())
        MobileAds.initialize(context)
        adView.loadAd(AdRequest.Builder().build())
    }
    DisposableEffect(adView) {
        onDispose(adView::destroy)
    }
    AndroidView(factory = { adView }, modifier = modifier.fillMaxWidth())
}
