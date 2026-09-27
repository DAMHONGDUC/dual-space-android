package com.dd.dual.space.core.logging

import android.util.Log
import com.dd.dual.space.BuildConfig

object AppLogger {
    private const val tag = "ParallelApp"

    // Release builds keep only event names and error types; payloads can name the user's games and accounts.
    private val logsDetails: Boolean = BuildConfig.DEBUG

    fun action(name: String, data: Map<String, Any?>) {
        if (logsDetails) Log.i(tag, "action=$name data=$data")
    }

    fun success(name: String, data: Map<String, Any?>) {
        if (logsDetails) Log.i(tag, "success=$name data=$data")
    }

    fun error(name: String, throwable: Throwable, data: Map<String, Any?>) {
        if (logsDetails) {
            Log.e(tag, "error=$name data=$data", throwable)
        } else {
            Log.e(tag, "error=$name type=${throwable.javaClass.simpleName}")
        }
    }
}
