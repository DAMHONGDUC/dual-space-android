package com.dd.the.universe.core.system.pm.installer;

import com.dd.the.universe.core.env.BEnvironment;
import com.dd.the.universe.core.system.pm.BPackageSettings;
import com.dd.the.universe.entity.pm.InstallOption;
import com.dd.the.universe.utils.BzFileUtils;


public class RemoveAppExecutor implements Executor {
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        BzFileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        return 0;
    }
}
