package com.duplicateapp.theuniverse.fake.service;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

import universeproxy.android.os.BRIVibratorManagerServiceStub;
import universeproxy.android.os.BRServiceManager;
import universeproxy.com.android.internal.os.BRIVibratorServiceStub;
import com.duplicateapp.theuniverse.fake.hook.BinderInvocationStub;
import com.duplicateapp.theuniverse.utils.MethodParameterUtils;
import com.duplicateapp.theuniverse.utils.compat.BuildCompat;


public class IVibratorServiceProxy extends BinderInvocationStub {
    private static String NAME;
    static {
        if (BuildCompat.isS()) {
            NAME = "vibrator_manager";
        } else {
            NAME = Context.VIBRATOR_SERVICE;
        }
    }

    public IVibratorServiceProxy() {
        super(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected Object getWho() {
        IBinder service = BRServiceManager.get().getService(NAME);
        if (BuildCompat.isS()) {
            return BRIVibratorManagerServiceStub.get().asInterface(service);
        }
        return BRIVibratorServiceStub.get().asInterface(service);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(NAME);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstUid(args);
        MethodParameterUtils.replaceFirstAppPkg(args);
        return super.invoke(proxy, method, args);
    }
}
