package com.duplicateapp.gamespace.features.update.domain

object ForceUpdatePolicy {
    const val minimumImmediatePriority = 4

    fun requiresUpdate(isAvailable: Boolean, isImmediateAllowed: Boolean, priority: Int): Boolean =
        isAvailable && isImmediateAllowed && priority >= minimumImmediatePriority
}
