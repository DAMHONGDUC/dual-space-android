package com.dd.the.universe.core.system.pm;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.os.Parcel;
import android.os.Process;
import android.util.ArrayMap;
import android.util.AtomicFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dd.the.universe.TheUniverseCore;
import com.dd.the.universe.core.env.BEnvironment;
import com.dd.the.universe.core.system.BProcessManagerService;
import com.dd.the.universe.core.system.user.BUserHandle;
import com.dd.the.universe.core.system.pm.installer.PackageDirectoryTransaction;
import com.dd.the.universe.entity.pm.InstallOption;
import com.dd.the.universe.utils.BzFileUtils;
import com.dd.the.universe.utils.Slog;
import com.dd.the.universe.utils.compat.PackageParserCompat;


 class Settings {
    public static final String TAG = "Settings";

    final ArrayMap<String, BPackageSettings> mPackages = new ArrayMap<>();
    private final Map<String, Integer> mAppIds = new HashMap<>();
    private final Map<String, SharedUserSetting> mSharedUsers = SharedUserSetting.sSharedUsers;
    private int mCurrUid = 0;

    public Settings() {
        synchronized (mPackages) {
            loadUidLP();
            SharedUserSetting.loadSharedUsers();
        }
    }

    BPackageSettings getPackageLPw(String name, PackageParser.Package aPackage, InstallOption installOption) {
        BPackageSettings pkgSettings;
        BPackageSettings origSettings = new BPackageSettings();
        origSettings.pkg = new BPackage(aPackage);
        origSettings.pkg.installOption = installOption;
        origSettings.installOption = installOption;
        origSettings.pkg.mExtras = origSettings;
        origSettings.pkg.applicationInfo = PackageManagerCompat.generateApplicationInfo(origSettings.pkg, 0, BPackageUserState.create(), 0);
        synchronized (mPackages) {
            pkgSettings = mPackages.get(name);
            if (pkgSettings != null) {
                origSettings.appId = pkgSettings.appId;
                origSettings.userState = pkgSettings.userState;
            } else {
                boolean b = registerAppIdLPw(origSettings);
                if (!b) {
                    throw new RuntimeException("registerAppIdLPw err.");
                }
            }
        }
        return origSettings;
    }

    boolean registerAppIdLPw(BPackageSettings p) {
        boolean createdNew = false;
        String sharedUserId = p.pkg.mSharedUserId;
        SharedUserSetting sharedUserSetting = null;
        if (sharedUserId != null) {
            sharedUserSetting = mSharedUsers.get(sharedUserId);
            if (sharedUserSetting == null) {
                sharedUserSetting = new SharedUserSetting(sharedUserId);
                sharedUserSetting.userId = acquireAndRegisterNewAppIdLPw(p);
                mSharedUsers.put(sharedUserId, sharedUserSetting);
            }
        }
        if (sharedUserSetting != null) {
            p.appId = sharedUserSetting.userId;
            Slog.d(TAG, p.pkg.packageName + " sharedUserId = " + sharedUserId + ", setAppId = " + p.appId);
        }
        if (p.appId == 0) {
            
            p.appId = acquireAndRegisterNewAppIdLPw(p);
        }
        if (p.appId < 0) {
            createdNew = false;




        } else {
            createdNew = true;
        }
        saveUidLP();
        SharedUserSetting.saveSharedUsers();
        return createdNew;
    }

    private int acquireAndRegisterNewAppIdLPw(BPackageSettings obj) {
        
        Integer integer = mAppIds.get(obj.pkg.packageName);
        if (integer != null)
            return Process.FIRST_APPLICATION_UID + integer;

        if (mCurrUid >= Process.LAST_APPLICATION_UID) {
            return -1;
        }
        mCurrUid++;
        mAppIds.put(obj.pkg.packageName, mCurrUid);
        return Process.FIRST_APPLICATION_UID + mCurrUid;
    }

    private void saveUidLP() {
        Parcel parcel = Parcel.obtain();
        FileOutputStream fileOutputStream = null;
        AtomicFile atomicFile = new AtomicFile(BEnvironment.getUidConf());
        try {
            Set<String> pkgName = mPackages.keySet();
            for (String s : new HashSet<>(mAppIds.keySet())) {
                if (!pkgName.contains(s)) {
                    mAppIds.remove(s);
                }
            }
            parcel.writeInt(mCurrUid);
            parcel.writeMap(mAppIds);

            fileOutputStream = atomicFile.startWrite();
            BzFileUtils.writeParcelToOutput(parcel, fileOutputStream);
            atomicFile.finishWrite(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
            atomicFile.failWrite(fileOutputStream);
        } finally {
            parcel.recycle();
        }
    }

    private void loadUidLP() {
        Parcel parcel = Parcel.obtain();
        try {
            byte[] uidBytes = BzFileUtils.toByteArray(BEnvironment.getUidConf());
            if (uidBytes == null || uidBytes.length == 0) {
                
                return;
            }
            
            parcel.unmarshall(uidBytes, 0, uidBytes.length);
            parcel.setDataPosition(0);

            mCurrUid = parcel.readInt();
            HashMap hashMap = parcel.readHashMap(HashMap.class.getClassLoader());
            synchronized (mAppIds) {
                mAppIds.clear();
                mAppIds.putAll(hashMap);
            }
        } catch (Exception e) {
            // Keep the unreadable file for recovery; app ids are rebuilt from package settings after scan.
            File uidConf = BEnvironment.getUidConf();
            File backup = new File(uidConf.getParentFile(), uidConf.getName() + ".corrupt-" + System.currentTimeMillis());
            boolean preserved = uidConf.renameTo(backup);
            Slog.e(TAG, "loadUidLP: unreadable uid config, preserved=" + preserved, e);
            mCurrUid = 0;
            synchronized (mAppIds) {
                mAppIds.clear();
            }
        } finally {
            parcel.recycle();
        }
    }

    public void scanPackage() {
        synchronized (mPackages) {
            File appRootDir = BEnvironment.getAppRootDir();
            BzFileUtils.mkdirs(appRootDir);
            File[] apps = appRootDir.listFiles();
            if (apps == null) {
                return;
            }
            for (File app : apps) {
                if (!app.isDirectory() || PackageDirectoryTransaction.isTransientDirectory(app.getName())) {
                    continue;
                }
                // Finishes or rolls back a binary swap that was interrupted by process death.
                new PackageDirectoryTransaction(app).recover();
                scanPackage(app.getName());
            }
            for (File app : apps) {
                String name = app.getName();
                if (name.endsWith(PackageDirectoryTransaction.PREVIOUS_SUFFIX)) {
                    String packageName = name.substring(0, name.length() - PackageDirectoryTransaction.PREVIOUS_SUFFIX.length());
                    new PackageDirectoryTransaction(BEnvironment.getAppDir(packageName)).recover();
                    scanPackage(packageName);
                }
            }
            reconcileAppIdsLP();
        }
    }

    // Prevents a rebuilt uid table from handing out an app id that a loaded package already owns.
    private void reconcileAppIdsLP() {
        HashSet<Integer> assigned = new HashSet<>();
        for (BPackageSettings settings : mPackages.values()) {
            assigned.add(settings.appId);
            int offset = settings.appId - Process.FIRST_APPLICATION_UID;
            if (offset > 0 && !mAppIds.containsKey(settings.pkg.packageName)) {
                mAppIds.put(settings.pkg.packageName, offset);
            }
        }
        for (SharedUserSetting sharedUser : mSharedUsers.values()) {
            assigned.add(sharedUser.userId);
        }
        int floor = AppIdRecovery.nextOffsetFloor(mCurrUid, assigned, Process.FIRST_APPLICATION_UID);
        if (floor != mCurrUid) {
            Slog.w(TAG, "reconcileAppIdsLP: advancing uid offset " + mCurrUid + " -> " + floor);
            mCurrUid = floor;
        }
        saveUidLP();
    }

    public void scanPackage(String packageName) {
        synchronized (mPackages) {
            updatePackageLP(BEnvironment.getAppDir(packageName));
        }
    }

    private void updatePackageLP(File app) {
        String packageName = app.getName();
        Parcel packageSettingsIn = Parcel.obtain();
        File packageConf = BEnvironment.getPackageConf(packageName);
        try {
            byte[] bPackageSettingsBytes = BzFileUtils.toByteArray(packageConf);

            packageSettingsIn.unmarshall(bPackageSettingsBytes, 0, bPackageSettingsBytes.length);
            packageSettingsIn.setDataPosition(0);

            BPackageSettings bPackageSettings = new BPackageSettings(packageSettingsIn);
            bPackageSettings.pkg.mExtras = bPackageSettings;
            if (bPackageSettings.installOption.isFlag(InstallOption.FLAG_SYSTEM)) {
                PackageInfo packageInfo = TheUniverseCore.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
                String currPackageSourcePath = packageInfo.applicationInfo.sourceDir;
                if (!currPackageSourcePath.equals(bPackageSettings.pkg.baseCodePath)) {
                    
                    BProcessManagerService.get().killAllByPackageName(bPackageSettings.pkg.packageName);
                    BPackageSettings newPkg = reInstallBySystem(packageInfo, bPackageSettings.installOption);
                    bPackageSettings.pkg = newPkg.pkg;
                }
            } else {
                bPackageSettings.pkg.applicationInfo = PackageManagerCompat.generateApplicationInfo(bPackageSettings.pkg, 0, BPackageUserState.create(), 0);
            }
            bPackageSettings.save();
            mPackages.put(bPackageSettings.pkg.packageName, bPackageSettings);
            Slog.d(TAG, "loaded Package: " + packageName);
        } catch (PackageManager.NameNotFoundException e) {
            // The original game is not installed right now; keep every copy on disk and load it again
            // once the game returns instead of deleting the user's copies.
            removePackage(packageName);
            Slog.w(TAG, "host package missing, copy kept on disk: " + packageName);
        } catch (Throwable e) {
            Slog.e(TAG, "bad Package: " + packageName, e);
            removePackage(packageName);
            BProcessManagerService.get().killAllByPackageName(packageName);
            File quarantined = PackageQuarantine.move(app, new File(BEnvironment.getSystemDir(), "quarantine"), System.currentTimeMillis());
            Slog.e(TAG, "bad Package quarantined: " + packageName + " -> " + quarantined);
            BPackageManagerService.get().onPackageUninstalled(packageName, true, BUserHandle.USER_ALL);
        } finally {
            packageSettingsIn.recycle();
        }
    }

    private BPackageSettings reInstallBySystem(PackageInfo systemPackageInfo, InstallOption option) throws Exception {
        Slog.d(TAG, "reInstallBySystem: " + systemPackageInfo.packageName);
        PackageParser.Package aPackage = parserApk(systemPackageInfo.applicationInfo.sourceDir);
        if (aPackage == null) {
            throw new RuntimeException("parser apk error.");
        }
        aPackage.applicationInfo = TheUniverseCore.getPackageManager().getPackageInfo(aPackage.packageName, 0).applicationInfo;
        return getPackageLPw(aPackage.packageName, aPackage, option);
    }

    public void removePackage(String packageName) {
        mPackages.remove(packageName);
    }

    private PackageParser.Package parserApk(String file) {
        try {
            PackageParser parser = PackageParserCompat.createParser(new File(file));
            PackageParser.Package aPackage = PackageParserCompat.parsePackage(parser, new File(file), 0);
            PackageParserCompat.collectCertificates(parser, aPackage, 0);
            return aPackage;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}
