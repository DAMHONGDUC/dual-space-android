package com.duplicateapp.gamespace.features.workspace.domain

interface ProfileProvisioner {
    fun status(): ProfileProvisioningStatus
    fun requestCreation(): Boolean
}

enum class ProfileProvisioningStatus {
    available,
    alreadyCreated,
    unsupported,
}
