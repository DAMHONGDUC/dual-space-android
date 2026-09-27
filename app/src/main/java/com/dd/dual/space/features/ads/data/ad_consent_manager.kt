package com.dd.dual.space.features.ads.data

import android.app.Activity
import com.dd.dual.space.core.logging.AppLogger
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class AdConsentManager(private val activity: Activity) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)

    fun request(onStateChanged: (AdConsentState) -> Unit) {
        AppLogger.action("request_ad_consent", emptyMap())
        val parameters = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (error != null) AppLogger.error("show_ad_consent", IllegalStateException(error.message), mapOf("code" to error.errorCode))
                    else AppLogger.success("show_ad_consent", emptyMap())
                    onStateChanged(currentState())
                }
            },
            { error ->
                AppLogger.error("request_ad_consent", IllegalStateException(error.message), mapOf("code" to error.errorCode))
                onStateChanged(currentState())
            },
        )
    }

    fun showPrivacyOptions(onStateChanged: (AdConsentState) -> Unit) {
        AppLogger.action("show_ad_privacy_options", emptyMap())
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) AppLogger.error("show_ad_privacy_options", IllegalStateException(error.message), mapOf("code" to error.errorCode))
            else AppLogger.success("show_ad_privacy_options", emptyMap())
            onStateChanged(currentState())
        }
    }

    private fun currentState(): AdConsentState = AdConsentState(
        canRequestAds = consentInformation.canRequestAds(),
        privacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED,
    )
}

data class AdConsentState(
    val canRequestAds: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
)
