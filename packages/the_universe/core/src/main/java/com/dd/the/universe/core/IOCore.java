package com.dd.the.universe.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.dd.the.universe.TheUniverseCore;

import com.dd.the.universe.core.env.BEnvironment;
import com.dd.the.universe.utils.BzFileUtils;
import com.dd.the.universe.utils.TrieTree;


@SuppressLint("SdCardPath")
public class IOCore {
    public static final String TAG = "IOCore";

    private static final IOCore sIOCore = new IOCore();
    private static final TrieTree mTrieTree = new TrieTree();
    private static final TrieTree sBlackTree = new TrieTree();
    private final Map<String, String> mRedirectMap = new LinkedHashMap<>();

    private static final Map<String, Map<String, String>> sCachePackageRedirect = new HashMap<>();

    public static IOCore get() {
        return sIOCore;
    }

    
    public synchronized void addRedirect(String origPath, String redirectPath) {
        if (TextUtils.isEmpty(origPath) || TextUtils.isEmpty(redirectPath) || mRedirectMap.get(origPath) != null)
            return;
        
        mTrieTree.add(origPath);
        mRedirectMap.put(origPath, redirectPath);
        File redirectFile = new File(redirectPath);
        if (!redirectFile.exists()) {
            BzFileUtils.mkdirs(redirectPath);
        }
        NativeCore.addIORule(origPath, redirectPath);
    }

    public synchronized void addBlackRedirect(String path) {
        if (TextUtils.isEmpty(path))
            return;
        sBlackTree.add(path);
    }

    public synchronized String redirectPath(String path) {
        if (path == null || path.isEmpty())
            return path;
        if (path.contains("/theuniverse/")) {
            return path;
        }
        String search = sBlackTree.search(path);
        if (!TextUtils.isEmpty(search))
            return path;

        
        String key = mTrieTree.search(path);
        if (!TextUtils.isEmpty(key))
            path = Objects.requireNonNull(mRedirectMap.get(key)) + path.substring(key.length());

        return path;
    }

    public File redirectPath(File path) {
        if (path == null)
            return null;
        String pathStr = path.getAbsolutePath();
        return new File(redirectPath(pathStr));
    }

    public String redirectPath(String path, Map<String, String> rule) {
        if (path == null || path.isEmpty())
            return path;
        String match = null;
        for (String key : rule.keySet()) {
            if (key == null || key.isEmpty() || !path.startsWith(key)) continue;
            boolean boundary = path.length() == key.length() || key.endsWith("/")
                    || path.charAt(key.length()) == '/';
            if (boundary && (match == null || key.length() > match.length())) match = key;
        }
        return match == null ? path : Objects.requireNonNull(rule.get(match)) + path.substring(match.length());
    }

    public File redirectPath(File path, Map<String, String> rule) {
        if (path == null)
            return null;
        String pathStr = path.getAbsolutePath();
        return new File(redirectPath(pathStr, rule));
    }

    

    public void enableRedirect(Context context) {
        Map<String, String> rule = new LinkedHashMap<>();
        Set<String> blackRule = new HashSet<>();
        String packageName = context.getPackageName();

        try {
            ApplicationInfo packageInfo = TheUniverseCore.getBPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA, TheUniverseCore.getUserId());
            int systemUserId = TheUniverseCore.getHostUserId();
            rule.put(String.format("/data/data/%s/lib", packageName), packageInfo.nativeLibraryDir);
            rule.put(String.format("/data/user/%d/%s/lib", systemUserId, packageName), packageInfo.nativeLibraryDir);

            rule.put(String.format("/data/data/%s", packageName), packageInfo.dataDir);
            rule.put(String.format("/data/user/%d/%s", systemUserId, packageName), packageInfo.dataDir);

            
            File profilesRoot = new File(BEnvironment.getVirtualRoot(), "profiles");
            BzFileUtils.mkdirs(profilesRoot.getAbsolutePath());
            
            rule.put("/data/misc/profiles", profilesRoot.getAbsolutePath());

            File profilesCurDir = new File(profilesRoot, String.format("cur/%d/%s", TheUniverseCore.getUserId(), packageName));
            File profilesRefDir = new File(profilesRoot, String.format("ref/%d/%s", TheUniverseCore.getUserId(), packageName));
            BzFileUtils.mkdirs(profilesCurDir.getAbsolutePath());
            BzFileUtils.mkdirs(profilesRefDir.getAbsolutePath());
            rule.put(String.format("/data/misc/profiles/cur/%d/%s", TheUniverseCore.getUserId(), packageName), profilesCurDir.getAbsolutePath());
            rule.put(String.format("/data/misc/profiles/ref/%d/%s", TheUniverseCore.getUserId(), packageName), profilesRefDir.getAbsolutePath());

            if (TheUniverseCore.getContext().getExternalCacheDir() != null && context.getExternalCacheDir() != null) {
                File external = BEnvironment.getExternalUserDir(TheUniverseCore.getUserId());

                
                rule.put("/sdcard", external.getAbsolutePath());
                rule.put(String.format("/storage/emulated/%d", systemUserId), external.getAbsolutePath());

                blackRule.add("/sdcard/Pictures");
                blackRule.add(String.format("/storage/emulated/%d/Pictures", systemUserId));
            }
            if (TheUniverseCore.get().isHideRoot()) {
                hideRoot(rule);
            }
            proc(rule);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String key : rule.keySet()) {
            get().addRedirect(key, rule.get(key));
        }
        for (String s : blackRule) {
            get().addBlackRedirect(s);
        }
        NativeCore.enableIO();
    }

    private void hideRoot(Map<String, String> rule) {
        rule.put("/system/app/Superuser.apk", "/system/app/Superuser.apk-fake");
        rule.put("/sbin/su", "/sbin/su-fake");
        rule.put("/system/bin/su", "/system/bin/su-fake");
        rule.put("/system/xbin/su", "/system/xbin/su-fake");
        rule.put("/data/local/xbin/su", "/data/local/xbin/su-fake");
        rule.put("/data/local/bin/su", "/data/local/bin/su-fake");
        rule.put("/system/sd/xbin/su", "/system/sd/xbin/su-fake");
        rule.put("/system/bin/failsafe/su", "/system/bin/failsafe/su-fake");
        rule.put("/data/local/su", "/data/local/su-fake");
        rule.put("/su/bin/su", "/su/bin/su-fake");
    }

    private void proc(Map<String, String> rule) {
        int appPid = TheUniverseCore.getAppPid();
        int pid = Process.myPid();
        String selfProc = "/proc/self/";
        String proc = "/proc/" + pid + "/";

        String cmdline = new File(BEnvironment.getProcDir(appPid), "cmdline").getAbsolutePath();
        rule.put(proc + "cmdline", cmdline);
        rule.put(selfProc + "cmdline", cmdline);
    }
}
