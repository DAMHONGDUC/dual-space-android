package com.dd.the.universe.core.system;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dd.the.universe.TheUniverseCore;
import com.dd.the.universe.core.IBActivityThread;
import com.dd.the.universe.core.env.BEnvironment;
import com.dd.the.universe.core.system.notification.BNotificationManagerService;
import com.dd.the.universe.core.system.pm.BPackageManagerService;
import com.dd.the.universe.core.system.user.BUserHandle;
import com.dd.the.universe.entity.AppConfig;
import com.dd.the.universe.proxy.ProxyManifest;
import com.dd.the.universe.utils.BzFileUtils;
import com.dd.the.universe.utils.Slog;
import com.dd.the.universe.utils.compat.ApplicationThreadCompat;
import com.dd.the.universe.utils.compat.BundleCompat;
import com.dd.the.universe.utils.provider.ProviderCall;


public class BProcessManagerService implements ISystemService {
    public static final String TAG = "BProcessManager";

    public static BProcessManagerService sBProcessManagerService = new BProcessManagerService();
    private final Map<Integer, Map<String, ProcessRecord>> mProcessMap = new HashMap<>();
    private final List<ProcessRecord> mPidsSelfLocked = new ArrayList<>();
    private final Object mProcessLock = new Object();

    public static BProcessManagerService get() {
        return sBProcessManagerService;
    }

    private static final long INIT_TIMEOUT_MILLIS = 10_000L;
    private static final int MAX_START_ATTEMPTS = 3;

    public ProcessRecord startProcessLocked(String packageName, String processName, int userId, int bpid, int callingPid) {
        ApplicationInfo info = BPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
        if (info == null)
            return null;
        int appId = BPackageManagerService.get().getAppId(packageName);
        int callingBUid = getBUidByPidOrPackageName(callingPid, packageName);
        return startProcess(info, processName, userId, appId, bpid, callingBUid);
    }

    /**
     * Reserves the slot under {@link #mProcessLock} but performs client initialization (a provider
     * call into the new process) outside it, so one hung guest cannot freeze every other lookup.
     */
    ProcessRecord startProcess(ApplicationInfo info, String processName, int userId, int appId, int requestedBPid, int callingBUid) {
        int buid = BUserHandle.getUid(userId, appId);
        for (int attempt = 0; attempt < MAX_START_ATTEMPTS; attempt++) {
            ProcessRecord pending = null;
            ProcessRecord app = null;
            synchronized (mProcessLock) {
                Map<String, ProcessRecord> bProcess = mProcessMap.get(buid);
                if (bProcess == null) {
                    bProcess = new HashMap<>();
                    mProcessMap.put(buid, bProcess);
                }
                if (requestedBPid == -1) {
                    ProcessRecord existing = bProcess.get(processName);
                    if (existing != null) {
                        if (existing.initializing) {
                            pending = existing;
                        } else if (isAttached(existing)) {
                            return existing;
                        } else {
                            bProcess.remove(processName);
                            mPidsSelfLocked.remove(existing);
                        }
                    }
                }
                if (pending == null) {
                    int bpid = requestedBPid != -1 ? requestedBPid : getUsingBPidL();
                    Slog.d(TAG, "init bUid = " + buid + ", bPid = " + bpid);
                    if (bpid == -1) {
                        throw new RuntimeException("No processes available");
                    }
                    app = new ProcessRecord(info, processName);
                    app.uid = Process.myUid();
                    app.bpid = bpid;
                    app.buid = appId;
                    app.callingBUid = callingBUid;
                    app.userId = userId;
                    app.initializing = true;
                    bProcess.put(processName, app);
                    mPidsSelfLocked.add(app);
                }
            }
            if (pending != null) {
                // Wait for the other caller's initialization without holding the registry lock.
                if (!pending.initLock.block(initTimeoutMillis())) {
                    Slog.w(TAG, "startProcess: timed out waiting for " + processName);
                    return null;
                }
                continue;
            }
            boolean initialized = false;
            boolean stillRegistered;
            try {
                initialized = initAppProcess(app);
            } catch (RuntimeException error) {
                Slog.e(TAG, "startProcess: init failed for " + processName, error);
            } finally {
                synchronized (mProcessLock) {
                    app.initializing = false;
                    Map<String, ProcessRecord> current = mProcessMap.get(buid);
                    stillRegistered = current != null && current.get(processName) == app;
                    if (!initialized || !stillRegistered) {
                        if (stillRegistered) {
                            current.remove(processName);
                            if (current.isEmpty()) {
                                mProcessMap.remove(buid);
                            }
                        }
                        mPidsSelfLocked.remove(app);
                    }
                }
                app.initLock.open();
            }
            if (!initialized || !stillRegistered) {
                // A kill during initialization wins; never hand back a process that was removed.
                if (initialized) {
                    app.kill();
                }
                return null;
            }
            app.pid = resolvePid(app);
            return app;
        }
        return null;
    }

    boolean isAttached(ProcessRecord app) {
        return app.bActivityThread != null && app.bActivityThread.asBinder().isBinderAlive();
    }

    long initTimeoutMillis() {
        return INIT_TIMEOUT_MILLIS;
    }

    int resolvePid(ProcessRecord app) {
        return getPid(TheUniverseCore.getContext(), ProxyManifest.getProcessName(app.bpid));
    }

    int getUsingBPidL() {
        ActivityManager manager = (ActivityManager) TheUniverseCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
        Set<Integer> usingPs = new HashSet<>();
        for (ProcessRecord record : mPidsSelfLocked) usingPs.add(record.bpid);
        if (runningAppProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                usingPs.add(parseBPid(runningAppProcess.processName));
            }
        }
        for (int i = 0; i < ProxyManifest.FREE_COUNT; i++) {
            if (usingPs.contains(i)) {
                continue;
            }
            return i;
        }
        return -1;
    }

    public void restartAppProcess(String packageName, String processName, int userId) {
        int callingPid = Binder.getCallingPid();
        if (findProcessByPid(callingPid) != null) {
            return;
        }
        String stubProcessName = getProcessName(TheUniverseCore.getContext(), callingPid);
        int bpid = parseBPid(stubProcessName);
        startProcessLocked(packageName, processName, userId, bpid, callingPid);
    }

    private int parseBPid(String stubProcessName) {
        String prefix;
        if (stubProcessName == null) {
            return -1;
        } else {
            prefix = TheUniverseCore.getHostPkg() + ":p";
        }
        if (stubProcessName.startsWith(prefix)) {
            try {
                return Integer.parseInt(stubProcessName.substring(prefix.length()));
            } catch (NumberFormatException e) {
                
            }
        }
        return -1;
    }

    boolean initAppProcess(ProcessRecord record) {
        Log.d(TAG, "initProcess: " + record.processName);
        AppConfig appConfig = record.getClientConfig();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConfig.KEY, appConfig);
        Bundle init = ProviderCall.callSafely(record.getProviderAuthority(), "_Black_|_init_process_", null, bundle);
        IBinder appThread = BundleCompat.getBinder(init, "_Black_|_client_");
        if (appThread == null || !appThread.isBinderAlive()) {
            return false;
        }
        attachClientL(record, appThread);
        if (record.bActivityThread == null) {
            return false;
        }

        createProc(record);
        return true;
    }

    private void attachClientL(final ProcessRecord app, final IBinder appThread) {
        IBActivityThread activityThread = IBActivityThread.Stub.asInterface(appThread);
        if (activityThread == null) {
            app.kill();
            return;
        }
        try {
            appThread.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Log.d(TAG, "App Died: " + app.processName);
                    appThread.unlinkToDeath(this, 0);
                    onProcessDie(app);
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.bActivityThread = activityThread;
        try {
            app.appThread = ApplicationThreadCompat.asInterface(activityThread.getActivityThread());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.initLock.open();
    }

    public void onProcessDie(ProcessRecord record) {
        synchronized (mProcessLock) {
            int key = BUserHandle.getUid(record.userId, record.buid);
            Map<String, ProcessRecord> process = mProcessMap.get(key);
            // A late death callback must not affect a replacement using the same slot.
            if (process == null || process.get(record.processName) != record) {
                mPidsSelfLocked.remove(record);
                return;
            }
            record.kill();
            process.remove(record.processName);
            if (process.isEmpty()) {
                mProcessMap.remove(key);
            }
            mPidsSelfLocked.remove(record);

            removeProc(record);
            BNotificationManagerService.get().deletePackageNotification(record.getPackageName(), record.userId);
        }
    }

    public ProcessRecord findProcessRecord(String packageName, String processName, int userId) {
        synchronized (mProcessLock) {
            int appId = BPackageManagerService.get().getAppId(packageName);
            int buid = BUserHandle.getUid(userId, appId);
            Map<String, ProcessRecord> processRecordMap = mProcessMap.get(buid);
            if (processRecordMap == null)
                return null;
            return processRecordMap.get(processName);
        }
    }

    public void killAllByPackageName(String packageName) {
        synchronized (mProcessLock) {
            List<ProcessRecord> tmp = new ArrayList<>(mPidsSelfLocked);
            int appId = BPackageManagerService.get().getAppId(packageName);
            for (ProcessRecord processRecord : mPidsSelfLocked) {
                int appId1 = BUserHandle.getAppId(processRecord.buid);
                if (appId == appId1) {
                    mProcessMap.remove(BUserHandle.getUid(processRecord.userId, processRecord.buid));
                    tmp.remove(processRecord);
                    processRecord.kill();
                }
            }
            mPidsSelfLocked.clear();
            mPidsSelfLocked.addAll(tmp);
        }
    }

    public void killPackageAsUser(String packageName, int userId) {
        synchronized (mProcessLock) {
            int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
            Map<String, ProcessRecord> process = mProcessMap.get(buid);
            if (process == null)
                return;
            for (ProcessRecord value : process.values()) {
                value.kill();
                mPidsSelfLocked.remove(value);
            }
            mProcessMap.remove(buid);
        }
    }

    public List<ProcessRecord> getPackageProcessAsUser(String packageName, int userId) {
        synchronized (mProcessLock) {
            int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
            Map<String, ProcessRecord> process = mProcessMap.get(buid);
            if (process == null)
                return new ArrayList<>();
            return new ArrayList<>(process.values());
        }
    }

    public int getBUidByPidOrPackageName(int pid, String packageName) {
        ProcessRecord callingProcess = findProcessByPid(pid);
        if (callingProcess == null) {
            return BPackageManagerService.get().getAppId(packageName);
        }
        return BUserHandle.getAppId(callingProcess.buid);
    }

    public int getUserIdByCallingPid(int callingPid) {
        ProcessRecord callingProcess = findProcessByPid(callingPid);
        if (callingProcess == null) {
            return 0;
        }
        return callingProcess.userId;
    }

    public ProcessRecord findProcessByPid(int pid) {
        synchronized (mProcessLock) {
            for (ProcessRecord processRecord : mPidsSelfLocked) {
                if (processRecord.pid == pid)
                    return processRecord;
            }
            return null;
        }
    }

    private static String getProcessName(Context context, int pid) {
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }
        if (processName == null) {
            throw new RuntimeException("processName = null");
        }
        return processName;
    }

    public static int getPid(Context context, String processName) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                if (runningAppProcess.processName.equals(processName)) {
                    return runningAppProcess.pid;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void createProc(ProcessRecord record) {
        File cmdline = new File(BEnvironment.getProcDir(record.bpid), "cmdline");
        try {
            BzFileUtils.writeToFile(record.processName.getBytes(), cmdline);
        } catch (IOException ignored) {
        }
    }

    private static void removeProc(ProcessRecord record) {
        BzFileUtils.deleteDir(BEnvironment.getProcDir(record.bpid));
    }

    @Override
    public void systemReady() {
        BzFileUtils.deleteDir(BEnvironment.getProcDir());
    }
}
