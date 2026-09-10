package com.duplicateapp.theuniverse.fake.service;

import java.lang.reflect.Method;
import java.util.List;

import universeproxy.com.android.internal.net.BRVpnConfig;
import universeproxy.com.android.internal.net.VpnConfigContext;
import com.duplicateapp.theuniverse.TheUniverseCore;
import com.duplicateapp.theuniverse.app.BActivityThread;
import com.duplicateapp.theuniverse.fake.hook.MethodHook;
import com.duplicateapp.theuniverse.fake.hook.ProxyMethod;
import com.duplicateapp.theuniverse.proxy.ProxyVpnService;
import com.duplicateapp.theuniverse.utils.MethodParameterUtils;


public class VpnCommonProxy {
    @ProxyMethod("setVpnPackageAuthorization")
    public static class setVpnPackageAuthorization extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("prepareVpn")
    public static class PrepareVpn extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("establishVpn")
    public static class establishVpn extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            VpnConfigContext vpnConfigContext = BRVpnConfig.get(args[0]);
            vpnConfigContext._set_user(ProxyVpnService.class.getName());

            handlePackage(vpnConfigContext.allowedApplications());
            handlePackage(vpnConfigContext.disallowedApplications());
            return method.invoke(who, args);
        }

        private void handlePackage(List<String> applications) {
            if (applications == null)
                return;
            if (applications.contains(BActivityThread.getAppPackageName())) {
                applications.add(TheUniverseCore.getHostPkg());
            }
        }
    }

}
