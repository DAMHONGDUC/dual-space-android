package com.duplicateapp.theuniverse.fake.service;

import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.ArrayList;

import universeproxy.android.content.pm.BRUserInfo;
import universeproxy.android.os.BRIUserManagerStub;
import universeproxy.android.os.BRServiceManager;
import com.duplicateapp.theuniverse.TheUniverseCore;
import com.duplicateapp.theuniverse.app.BActivityThread;
import com.duplicateapp.theuniverse.fake.hook.BinderInvocationStub;
import com.duplicateapp.theuniverse.fake.hook.MethodHook;
import com.duplicateapp.theuniverse.fake.hook.ProxyMethod;


public class IUserManagerProxy extends BinderInvocationStub {
    public IUserManagerProxy() {
        super(BRServiceManager.get().getService(Context.USER_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIUserManagerStub.get().asInterface(BRServiceManager.get().getService(Context.USER_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.USER_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getApplicationRestrictions")
    public static class GetApplicationRestrictions extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            // Privacy-first: never delegate per-app restrictions to the host.
            // Sandboxed apps normally do not have enterprise restrictions, and
            // Chromium-based browsers crash if the framework throws here.
            return new Bundle();
        }
    }

    @ProxyMethod("getApplicationRestrictionsForUser")
    public static class GetApplicationRestrictionsForUser extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            // Android 14+/16 paths may call the "*ForUser" variant directly.
            // Return an empty bundle rather than leaking host policy state or
            // surfacing a SecurityException for the sandbox package/uid pair.
            return new Bundle();
        }
    }

    @ProxyMethod("getProfileParent")
    public static class GetProfileParent extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Object universe = BRUserInfo.get()._new(BActivityThread.getUserId(), "TheUniverse", BRUserInfo.get().FLAG_PRIMARY());
            return universe;
        }
    }

    @ProxyMethod("getUsers")
    public static class getUsers extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return new ArrayList<>();
        }
    }
}
