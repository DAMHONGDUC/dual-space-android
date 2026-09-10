package com.duplicateapp.theuniverse.fake.service;

import android.content.Context;

import universeproxy.android.os.BRIPowerManagerStub;
import universeproxy.android.os.BRServiceManager;
import com.duplicateapp.theuniverse.fake.hook.BinderInvocationStub;
import com.duplicateapp.theuniverse.fake.service.base.ValueMethodProxy;


public class IPowerManagerProxy extends BinderInvocationStub {
    public IPowerManagerProxy() {
        super(BRServiceManager.get().getService(Context.POWER_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIPowerManagerStub.get().asInterface(BRServiceManager.get().getService(Context.POWER_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.POWER_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("acquireWakeLock", 0));
        addMethodHook(new ValueMethodProxy("acquireWakeLockWithUid", 0));
        addMethodHook(new ValueMethodProxy("releaseWakeLock", 0));
        addMethodHook(new ValueMethodProxy("updateWakeLockWorkSource", 0));
        addMethodHook(new ValueMethodProxy("isWakeLockLevelSupported", true));
    }
}
