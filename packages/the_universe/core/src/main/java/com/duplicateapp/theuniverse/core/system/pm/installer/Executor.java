package com.duplicateapp.theuniverse.core.system.pm.installer;

import com.duplicateapp.theuniverse.core.system.pm.BPackageSettings;
import com.duplicateapp.theuniverse.entity.pm.InstallOption;


public interface Executor {
    public static final String TAG = "InstallExecutor";

    int exec(BPackageSettings ps, InstallOption option, int userId);
}
