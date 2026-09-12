package com.duplicateapp.gamespace.features.workspace.data

import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioner
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioningStatus

class VirtualProfileProvisioner : ProfileProvisioner {
    override fun status(): ProfileProvisioningStatus = ProfileProvisioningStatus.alreadyCreated
    override fun requestCreation(): Boolean = true
}
