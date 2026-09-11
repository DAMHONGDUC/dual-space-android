package com.duplicateapp.gamespace.features.premium.domain

enum class PremiumStatus {
    unknown,
    inactive,
    active,
}

data class PremiumAccess(
    val status: PremiumStatus,
    val userId: String? = null,
) {
    val removesAds: Boolean = status == PremiumStatus.active
    val showsAds: Boolean = status == PremiumStatus.inactive
}

interface PremiumRepository {
    suspend fun refresh(): PremiumAccess
    suspend fun identify(userId: String): PremiumAccess
    suspend fun logOut(): PremiumAccess
    suspend fun restore(): PremiumAccess
}
