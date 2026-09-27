package com.dd.dual.space.features.workspace.domain

interface ProfileProvisioner {
    fun status(): ProfileProvisioningStatus
    fun requestCreation(): Boolean
}

enum class ProfileProvisioningStatus {
    available,
    alreadyCreated,
    unsupported,
}
