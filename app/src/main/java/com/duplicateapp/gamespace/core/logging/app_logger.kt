package com.duplicateapp.gamespace.core.logging

import android.util.Log

object app_logger {
    private const val tag = "GameSpace"

    fun action(name: String, data: Map<String, Any?>) {
        Log.i(tag, "action=$name data=$data")
    }

    fun success(name: String, data: Map<String, Any?>) {
        Log.i(tag, "success=$name data=$data")
    }

    fun error(name: String, throwable: Throwable, data: Map<String, Any?>) {
        Log.e(tag, "error=$name data=$data", throwable)
    }
}
