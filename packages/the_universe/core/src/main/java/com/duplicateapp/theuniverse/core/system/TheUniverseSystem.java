package com.duplicateapp.theuniverse.core.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.duplicateapp.theuniverse.TheUniverseCore;
import com.duplicateapp.theuniverse.core.env.AppSystemEnv;
import com.duplicateapp.theuniverse.core.env.BEnvironment;
import com.duplicateapp.theuniverse.core.system.accounts.BAccountManagerService;
import com.duplicateapp.theuniverse.core.system.am.BActivityManagerService;
import com.duplicateapp.theuniverse.core.system.am.BJobManagerService;
import com.duplicateapp.theuniverse.core.system.location.BLocationManagerService;
import com.duplicateapp.theuniverse.core.system.notification.BNotificationManagerService;
import com.duplicateapp.theuniverse.core.system.os.BStorageManagerService;
import com.duplicateapp.theuniverse.core.system.pm.BPackageInstallerService;
import com.duplicateapp.theuniverse.core.system.pm.BPackageManagerService;

import com.duplicateapp.theuniverse.core.system.user.BUserHandle;
import com.duplicateapp.theuniverse.core.system.user.BUserManagerService;
import com.duplicateapp.theuniverse.entity.pm.InstallOption;
import com.duplicateapp.theuniverse.utils.BzFileUtils;

import com.duplicateapp.theuniverse.core.system.JarManager;


public class TheUniverseSystem {
    private static TheUniverseSystem sTheUniverseSystem;
    private final List<ISystemService> mServices = new ArrayList<>();
    private final static AtomicBoolean isStartup = new AtomicBoolean(false);

    public static TheUniverseSystem getSystem() {
        if (sTheUniverseSystem == null) {
            synchronized (TheUniverseSystem.class) {
                if (sTheUniverseSystem == null) {
                    sTheUniverseSystem = new TheUniverseSystem();
                }
            }
        }
        return sTheUniverseSystem;
    }

    public void startup() {
        if (isStartup.getAndSet(true))
            return;
        BEnvironment.load();

        mServices.add(BPackageManagerService.get());
        mServices.add(BUserManagerService.get());
        mServices.add(BActivityManagerService.get());
        mServices.add(BJobManagerService.get());
        mServices.add(BStorageManagerService.get());
        mServices.add(BPackageInstallerService.get());

        mServices.add(BProcessManagerService.get());
        mServices.add(BAccountManagerService.get());
        mServices.add(BLocationManagerService.get());
        mServices.add(BNotificationManagerService.get());

        for (ISystemService service : mServices) {
            service.systemReady();
        }

        List<String> preInstallPackages = AppSystemEnv.getPreInstallPackages();
        for (String preInstallPackage : preInstallPackages) {
            try {
                if (!BPackageManagerService.get().isInstalled(preInstallPackage, BUserHandle.USER_ALL)) {
                    PackageInfo packageInfo = TheUniverseCore.getPackageManager().getPackageInfo(preInstallPackage, 0);
                    BPackageManagerService.get().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), BUserHandle.USER_ALL);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        
        JarManager.getInstance().initializeAsync();
        
        
     
    }
}
