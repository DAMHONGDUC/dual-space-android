package com.dd.the.universe.fake.service;

import static com.dd.the.universe.app.BActivityThread.getUid;

import java.lang.reflect.Method;

import universeproxy.android.os.BRINetworkManagementServiceStub;
import universeproxy.android.os.BRServiceManager;
import com.dd.the.universe.fake.hook.BinderInvocationStub;
import com.dd.the.universe.fake.hook.MethodHook;
import com.dd.the.universe.fake.hook.ProxyMethod;
import com.dd.the.universe.fake.service.base.UidMethodProxy;
import com.dd.the.universe.utils.MethodParameterUtils;
import com.dd.the.universe.utils.Slog;


public class INetworkManagementServiceProxy extends BinderInvocationStub {
    public static final String NAME = "network_management";

    public INetworkManagementServiceProxy() {
        super(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected Object getWho() {
        return BRINetworkManagementServiceStub.get().asInterface(BRServiceManager.get().getService(NAME));
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
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new UidMethodProxy("setUidCleartextNetworkPolicy", 0));
        addMethodHook(new UidMethodProxy("setUidMeteredNetworkBlacklist", 0));
        addMethodHook(new UidMethodProxy("setUidMeteredNetworkWhitelist", 0));
    }

    @ProxyMethod("getNetworkStatsUidDetail")
    public static class getNetworkStatsUidDetail extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstUid(args);
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
