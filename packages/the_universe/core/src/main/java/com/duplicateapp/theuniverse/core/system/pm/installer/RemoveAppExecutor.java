package com.duplicateapp.theuniverse.core.system.pm.installer;

import com.duplicateapp.theuniverse.core.env.BEnvironment;
import com.duplicateapp.theuniverse.core.system.pm.BPackageSettings;
import com.duplicateapp.theuniverse.entity.pm.InstallOption;
import com.duplicateapp.theuniverse.utils.BzFileUtils;


public class RemoveAppExecutor implements Executor {
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        BzFileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        return 0;
    }
}
