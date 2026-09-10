package com.duplicateapp.theuniverse.fake.service;

import android.content.ComponentName;

import java.lang.reflect.Method;

import universeproxy.android.os.BRServiceManager;
import universeproxy.android.view.BRIAutoFillManagerStub;
import com.duplicateapp.theuniverse.TheUniverseCore;
import com.duplicateapp.theuniverse.app.BActivityThread;
import com.duplicateapp.theuniverse.fake.hook.BinderInvocationStub;
import com.duplicateapp.theuniverse.fake.hook.MethodHook;
import com.duplicateapp.theuniverse.fake.hook.ProxyMethod;
import com.duplicateapp.theuniverse.proxy.ProxyManifest;


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
