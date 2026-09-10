// IBPackageInstallerService.aidl
package com.duplicateapp.theuniverse.core.system.pm;

import com.duplicateapp.theuniverse.core.system.pm.BPackageSettings;
import com.duplicateapp.theuniverse.entity.pm.InstallOption;

// Declare any non-default types here with import statements

interface IBPackageInstallerService {
    int installPackageAsUser(in BPackageSettings ps, int userId);
    int uninstallPackageAsUser(in BPackageSettings ps, boolean removeApp, int userId);
    int clearPackage(in BPackageSettings ps, int userId);
    int updatePackage(in BPackageSettings ps);
}
