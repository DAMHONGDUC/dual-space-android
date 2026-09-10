package com.duplicateapp.gamespace.features.workspace.domain

interface profile_provisioner {
    fun status(): profile_provisioning_status
    fun request_creation(): Boolean
}

enum class profile_provisioning_status {
    available,
    already_created,
    unsupported,
}
