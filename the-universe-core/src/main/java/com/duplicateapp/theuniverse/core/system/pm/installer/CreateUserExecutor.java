package com.duplicateapp.theuniverse.core.system.pm.installer;

import com.duplicateapp.theuniverse.core.env.BEnvironment;
import com.duplicateapp.theuniverse.core.system.pm.BPackageSettings;
import com.duplicateapp.theuniverse.entity.pm.InstallOption;
import com.duplicateapp.theuniverse.utils.BzFileUtils;


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
