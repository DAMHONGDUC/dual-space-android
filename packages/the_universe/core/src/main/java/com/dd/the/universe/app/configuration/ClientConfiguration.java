package com.dd.the.universe.app.configuration;

import java.io.File;


public abstract class ClientConfiguration {

    public boolean isHideRoot() {
        return false;
    }



    public abstract String getHostPackageName();

    public boolean isEnableDaemonService() {
        return true;
    }

    public boolean isDisableFlagSecure() {
        return false;
    }

    
    public boolean requestInstallPackage(File file, int userId) {
        return false;
    }
}
