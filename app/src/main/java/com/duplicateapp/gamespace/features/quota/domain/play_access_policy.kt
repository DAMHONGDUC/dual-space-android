package com.duplicateapp.gamespace.features.quota.domain

import com.duplicateapp.gamespace.features.premium.domain.PremiumAccess

object PlayAccessPolicy {
    const val rewardedSeconds: Long = FreeQuotaPolicy.secondsPerHour

    fun canLaunch(
        usedSeconds: Long,
        rewardedBonusSeconds: Long,
        premiumAccess: PremiumAccess,
    ): Boolean = premiumAccess.hasUnlimitedPlayTime || remainingSeconds(usedSeconds, rewardedBonusSeconds) > 0L

    fun remainingSeconds(usedSeconds: Long, rewardedBonusSeconds: Long): Long {
        require(usedSeconds >= 0L)
        require(rewardedBonusSeconds >= 0L)
        return (FreeQuotaPolicy.monthlyLimitSeconds + rewardedBonusSeconds - usedSeconds).coerceAtLeast(0L)
    }
}
