package com.dd.the.universe.core.system.pm.installer;

import com.dd.the.universe.core.env.BEnvironment;
import com.dd.the.universe.core.system.pm.BPackageSettings;
import com.dd.the.universe.entity.pm.InstallOption;
import com.dd.the.universe.utils.BzFileUtils;


public class CreateUserExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        BzFileUtils.deleteDir(BEnvironment.getDataLibDir(packageName, userId));

        
        BzFileUtils.mkdirs(BEnvironment.getDataDir(packageName, userId));
        BzFileUtils.mkdirs(BEnvironment.getDataCacheDir(packageName, userId));
        BzFileUtils.mkdirs(BEnvironment.getDataFilesDir(packageName, userId));
        BzFileUtils.mkdirs(BEnvironment.getDataDatabasesDir(packageName, userId));
        BzFileUtils.mkdirs(BEnvironment.getDeDataDir(packageName, userId));








        return 0;
    }
}
