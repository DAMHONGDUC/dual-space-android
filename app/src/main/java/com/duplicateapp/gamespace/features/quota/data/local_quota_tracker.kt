package com.duplicateapp.gamespace.features.quota.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.quota.domain.FreeQuotaPolicy
import java.time.YearMonth

class LocalQuotaTracker(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    fun remainingHours(): Int {
        resetPeriodIfNeeded()
        return FreeQuotaPolicy.remainingWholeHours(preferences.getLong(usedSecondsKey, 0L))
    }

    fun canLaunch(): Boolean = remainingHours() > 0

    fun startSession() {
        AppLogger.action("start_quota_session", mapOf("remainingHours" to remainingHours()))
        preferences.edit { putLong(activeSinceKey, System.currentTimeMillis()) }
        AppLogger.success("start_quota_session", emptyMap())
    }

    fun settleSession() {
        resetPeriodIfNeeded()
        val activeSince: Long = preferences.getLong(activeSinceKey, 0L)
        if (activeSince == 0L) return
        val elapsedSeconds: Long = ((System.currentTimeMillis() - activeSince) / millisecondsPerSecond).coerceAtLeast(0L)
        val usedSeconds: Long = preferences.getLong(usedSecondsKey, 0L) + elapsedSeconds
        preferences.edit {
            putLong(usedSecondsKey, usedSeconds)
            remove(activeSinceKey)
        }
        AppLogger.success("settle_quota_session", mapOf("elapsedSeconds" to elapsedSeconds, "usedSeconds" to usedSeconds))
    }

    private fun resetPeriodIfNeeded() {
        val currentPeriod: String = YearMonth.now().toString()
        if (preferences.getString(periodKey, null) == currentPeriod) return
        preferences.edit {
            putString(periodKey, currentPeriod)
            putLong(usedSecondsKey, 0L)
            remove(activeSinceKey)
        }
        AppLogger.success("reset_quota_period", mapOf("period" to currentPeriod))
    }

    private companion object {
        const val preferencesName = "quota"
        const val periodKey = "period"
        const val usedSecondsKey = "used_seconds"
        const val activeSinceKey = "active_since"
        const val millisecondsPerSecond = 1_000L
    }
}
