package com.dd.the.universe.core.system.pm.installer;

import com.dd.the.universe.core.env.BEnvironment;
import com.dd.the.universe.core.system.pm.BPackageSettings;
import com.dd.the.universe.entity.pm.InstallOption;
import com.dd.the.universe.utils.BzFileUtils;


public class RemoveUserExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        // Report partial deletion so the caller keeps the copy registered instead of reusing its slot.
        boolean removed = BzFileUtils.deleteDirFully(BEnvironment.getDataDir(packageName, userId));
        removed &= BzFileUtils.deleteDirFully(BEnvironment.getDeDataDir(packageName, userId));
        removed &= BzFileUtils.deleteDirFully(BEnvironment.getExternalDataDir(packageName, userId));
        return removed ? 0 : -1;
    }
}
