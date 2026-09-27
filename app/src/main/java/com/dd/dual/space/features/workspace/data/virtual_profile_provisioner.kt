package com.dd.dual.space.features.workspace.data

import com.dd.dual.space.features.workspace.domain.ProfileProvisioner
import com.dd.dual.space.features.workspace.domain.ProfileProvisioningStatus

class VirtualProfileProvisioner : ProfileProvisioner {
    override fun status(): ProfileProvisioningStatus = ProfileProvisioningStatus.alreadyCreated
    override fun requestCreation(): Boolean = true
}
