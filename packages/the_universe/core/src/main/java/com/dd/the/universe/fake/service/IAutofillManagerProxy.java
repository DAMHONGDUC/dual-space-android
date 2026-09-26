package com.dd.the.universe.fake.service;

import android.content.ComponentName;

import java.lang.reflect.Method;

import universeproxy.android.os.BRServiceManager;
import universeproxy.android.view.BRIAutoFillManagerStub;
import com.dd.the.universe.TheUniverseCore;
import com.dd.the.universe.app.BActivityThread;
import com.dd.the.universe.fake.hook.BinderInvocationStub;
import com.dd.the.universe.fake.hook.MethodHook;
import com.dd.the.universe.fake.hook.ProxyMethod;
import com.dd.the.universe.proxy.ProxyManifest;


public class IAutofillManagerProxy extends BinderInvocationStub {
    public static final String TAG = "AutofillManagerStub";

    public IAutofillManagerProxy() {
        super(BRServiceManager.get().getService("autofill"));
    }

    @Override
    protected Object getWho() {
        return BRIAutoFillManagerStub.get().asInterface(BRServiceManager.get().getService("autofill"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("autofill");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("startSession")
    public static class StartSession extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null)
                        continue;
                    if (args[i] instanceof ComponentName) {
                        args[i] = new ComponentName(TheUniverseCore.getHostPkg(), ProxyManifest.getProxyActivity(TheUniverseCore.getAppPid()));
                    }
                }
            }
            return method.invoke(who, args);
        }
    }
}
