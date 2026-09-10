package com.duplicateapp.gamespace.features.quota.domain

import kotlin.math.max

object free_quota_policy {
    const val monthly_limit_hours = 180
    const val seconds_per_hour = 3_600L
    const val monthly_limit_seconds = monthly_limit_hours * seconds_per_hour

    fun remaining_seconds(used_seconds: Long): Long {
        return max(0L, monthly_limit_seconds - used_seconds)
    }

    fun remaining_whole_hours(used_seconds: Long): Int {
        return (remaining_seconds(used_seconds) / seconds_per_hour).toInt()
    }

    fun consumed_seconds(running_session_count: Int, elapsed_seconds: Long): Long {
        require(running_session_count >= 0)
        require(elapsed_seconds >= 0)
        return running_session_count * elapsed_seconds
    }
}
