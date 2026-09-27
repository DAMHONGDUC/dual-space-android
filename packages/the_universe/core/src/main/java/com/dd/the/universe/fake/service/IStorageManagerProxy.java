package com.dd.the.universe.fake.service;

import android.os.IInterface;
import android.os.storage.StorageVolume;

import java.lang.reflect.Method;

import universeproxy.android.os.BRServiceManager;
import universeproxy.android.os.mount.BRIMountServiceStub;
import universeproxy.android.os.storage.BRIStorageManagerStub;
import com.dd.the.universe.TheUniverseCore;
import com.dd.the.universe.app.BActivityThread;
import com.dd.the.universe.fake.hook.BinderInvocationStub;
import com.dd.the.universe.fake.hook.MethodHook;
import com.dd.the.universe.fake.hook.ProxyMethod;
import com.dd.the.universe.core.system.user.BUserHandle;
import com.dd.the.universe.utils.compat.BuildCompat;


public class IStorageManagerProxy extends BinderInvocationStub {

    public IStorageManagerProxy() {
        super(BRServiceManager.get().getService("mount"));
    }

    @Override
    protected Object getWho() {
        IInterface mount;
        if (BuildCompat.isOreo()) {
            mount = BRIStorageManagerStub.get().asInterface(BRServiceManager.get().getService("mount"));
        } else {
            mount = BRIMountServiceStub.get().asInterface(BRServiceManager.get().getService("mount"));
        }
        return mount;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("mount");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getVolumeList")
    public static class GetVolumeList extends MethodHook {

        private static Object invokeAsHost(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length >= 3) {
                args[0] = TheUniverseCore.getHostUid();
                args[1] = TheUniverseCore.getHostPkg();
            }
            if (args != null && args.length >= 4) {
                args[3] = BUserHandle.getUserId(android.os.Process.myUid());
            }
            return method.invoke(who, args);
        }

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args == null) {
                StorageVolume[] volumeList = TheUniverseCore.getBStorageManager().getVolumeList(BActivityThread.getBUid(), null, 0, BActivityThread.getUserId());
                if (volumeList == null) {
                    return method.invoke(who, args);
                }
                return volumeList;
            }
            try {
                int uid = (int) args[0];
                String packageName = (String) args[1];
                int flags = (int) args[2];
                StorageVolume[] volumeList = TheUniverseCore.getBStorageManager().getVolumeList(uid, packageName, flags, BActivityThread.getUserId());
                if (volumeList == null) {
                    return invokeAsHost(who, method, args);
                }
                return volumeList;
            } catch (Throwable t) {
                return invokeAsHost(who, method, args);
            }
        }
    }

    @ProxyMethod("mkdirs")
    public static class mkdirs extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }
}
