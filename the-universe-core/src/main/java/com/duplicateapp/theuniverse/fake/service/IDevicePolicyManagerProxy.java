package com.duplicateapp.theuniverse.fake.service;

import android.content.ComponentName;
import android.content.Context;

import java.lang.reflect.Method;

import universeproxy.android.app.admin.BRIDevicePolicyManagerStub;
import universeproxy.android.os.BRServiceManager;
import com.duplicateapp.theuniverse.fake.hook.BinderInvocationStub;
import com.duplicateapp.theuniverse.fake.hook.MethodHook;
import com.duplicateapp.theuniverse.fake.hook.ProxyMethod;
import com.duplicateapp.theuniverse.utils.MethodParameterUtils;


public class IDevicePolicyManagerProxy extends BinderInvocationStub {
    public IDevicePolicyManagerProxy() {
        super(BRServiceManager.get().getService(Context.DEVICE_POLICY_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIDevicePolicyManagerStub.get().asInterface(BRServiceManager.get().getService(Context.DEVICE_POLICY_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getStorageEncryptionStatus")
    public static class GetStorageEncryptionStatus extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getDeviceOwnerComponent")
    public static class GetDeviceOwnerComponent extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return new ComponentName("", "");
        }
    }

    @ProxyMethod("getDeviceOwnerName")
    public static class getDeviceOwnerName extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return "TheUniverse";
        }
    }

    @ProxyMethod("getProfileOwnerName")
    public static class getProfileOwnerName extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return "TheUniverse";
        }
    }

    @ProxyMethod("isDeviceProvisioned")
    public static class isDeviceProvisioned extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return true;
        }
    }
}
