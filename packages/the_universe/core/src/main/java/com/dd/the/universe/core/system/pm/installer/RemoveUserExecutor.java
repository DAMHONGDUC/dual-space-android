package com.dd.the.universe.core.system.pm.installer;

import com.dd.the.universe.core.env.BEnvironment;
import com.dd.the.universe.core.system.pm.BPackageSettings;
import com.dd.the.universe.entity.pm.InstallOption;
import com.dd.the.universe.utils.BzFileUtils;


public class RemoveUserExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        
        BzFileUtils.deleteDir(BEnvironment.getDataDir(packageName, userId));
        BzFileUtils.deleteDir(BEnvironment.getDeDataDir(packageName, userId));
        BzFileUtils.deleteDir(BEnvironment.getExternalDataDir(packageName, userId));
        return 0;
    }
}
