package com.dd.dual.space.features.virtualization.domain

interface VirtualGameRuntime {
    fun installFromDevice(packageName: String, virtualUserId: Int): VirtualRuntimeResult
    fun isInstalled(packageName: String, virtualUserId: Int): Boolean
    fun launch(packageName: String, virtualUserId: Int): VirtualRuntimeResult
    fun isRunning(packageName: String, virtualUserId: Int): Boolean = false
    fun isSourceInstalled(packageName: String): Boolean = true
    fun uninstall(packageName: String, virtualUserId: Int): VirtualRuntimeResult
}

sealed interface VirtualRuntimeResult {
    data object Success : VirtualRuntimeResult
    data class Failure(val message: String?, val kind: FailureKind = FailureKind.unknown) : VirtualRuntimeResult
}

enum class FailureKind {
    engineUnavailable,
    sourceMissing,
    installFailed,
    launchTimedOut,
    unknown,
}
