package com.duplicateapp.gamespace.features.quota.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duplicateapp.gamespace.core.logging.app_logger
import com.duplicateapp.gamespace.features.quota.domain.free_quota_policy
import java.time.YearMonth

class local_quota_tracker(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE)

    fun remaining_hours(): Int {
        reset_period_if_needed()
        return free_quota_policy.remaining_whole_hours(preferences.getLong(used_seconds_key, 0L))
    }

    fun can_launch(): Boolean = remaining_hours() > 0

    fun start_session() {
        app_logger.action("start_quota_session", mapOf("remainingHours" to remaining_hours()))
        preferences.edit { putLong(active_since_key, System.currentTimeMillis()) }
        app_logger.success("start_quota_session", emptyMap())
    }

    fun settle_session() {
        reset_period_if_needed()
        val active_since: Long = preferences.getLong(active_since_key, 0L)
        if (active_since == 0L) return
        val elapsed_seconds: Long = ((System.currentTimeMillis() - active_since) / milliseconds_per_second).coerceAtLeast(0L)
        val used_seconds: Long = preferences.getLong(used_seconds_key, 0L) + elapsed_seconds
        preferences.edit {
            putLong(used_seconds_key, used_seconds)
            remove(active_since_key)
        }
        app_logger.success("settle_quota_session", mapOf("elapsedSeconds" to elapsed_seconds, "usedSeconds" to used_seconds))
    }

    private fun reset_period_if_needed() {
        val current_period: String = YearMonth.now().toString()
        if (preferences.getString(period_key, null) == current_period) return
        preferences.edit {
            putString(period_key, current_period)
            putLong(used_seconds_key, 0L)
            remove(active_since_key)
        }
        app_logger.success("reset_quota_period", mapOf("period" to current_period))
    }

    private companion object {
        const val preferences_name = "quota"
        const val period_key = "period"
        const val used_seconds_key = "used_seconds"
        const val active_since_key = "active_since"
        const val milliseconds_per_second = 1_000L
    }
}
