package com.dd.the.universe.fake.service;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

import universeproxy.android.location.BRILocationManagerStub;
import universeproxy.android.os.BRServiceManager;
import com.dd.the.universe.app.BActivityThread;
import com.dd.the.universe.fake.hook.BinderInvocationStub;
import com.dd.the.universe.fake.hook.MethodHook;
import com.dd.the.universe.fake.hook.ProxyMethod;
import com.dd.the.universe.utils.MethodParameterUtils;

// The host holds no location permission, so guests never receive a location: reads return
// nothing, update registrations are dropped, and location is reported as off.
public class ILocationManagerProxy extends BinderInvocationStub {
    public static final String TAG = "ILocationManagerProxy";

    public ILocationManagerProxy() {
        super(BRServiceManager.get().getService(Context.LOCATION_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRILocationManagerStub.get().asInterface(BRServiceManager.get().getService(Context.LOCATION_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        MethodParameterUtils.replaceFirstAppPkg(args);

        String packageName = BActivityThread.getAppPackageName();
        if (packageName != null && packageName.equals("com.google.android.gms")) {

            if (method.getName().equals("getLastLocation") ||
                method.getName().equals("getLastKnownLocation") ||
                method.getName().equals("requestLocationUpdates")) {
                Log.w(TAG, "Blocking location request from Google Play Services to prevent crash");
                return null;
            }
        }

        // Handled here rather than per @ProxyMethod so OEM builds with extra overloads never
        // fall through to the host.
        if (method != null) {
            String n = method.getName();
            if ("getLastLocation".equals(n) || "getLastKnownLocation".equals(n)) {
                return null;
            }
            if ("requestLocationUpdates".equals(n) || "registerLocationListener".equals(n) || "getCurrentLocation".equals(n)) {
                return defaultReturn(method);
            }
        }

        return super.invoke(proxy, method, args);
    }

    private static Object defaultReturn(Method method) {
        if (method == null) {
            return null;
        }
        Class<?> rt = method.getReturnType();
        if (rt == null || rt == Void.TYPE) {
            return null;
        }
        if (rt == Boolean.TYPE) {
            return true;
        }
        if (rt == Integer.TYPE) {
            return 0;
        }
        if (rt == Long.TYPE) {
            return 0L;
        }
        if (rt == Float.TYPE) {
            return 0f;
        }
        if (rt == Double.TYPE) {
            return 0d;
        }
        if (rt == Short.TYPE) {
            return (short) 0;
        }
        if (rt == Byte.TYPE) {
            return (byte) 0;
        }
        if (rt == Character.TYPE) {
            return (char) 0;
        }
        return null;
    }

    @ProxyMethod("registerGnssStatusCallback")
    public static class RegisterGnssStatusCallback extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {

            return true;
        }
    }

    @ProxyMethod("removeUpdates")
    public static class RemoveUpdates extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(who, args);
            } catch (Exception e) {
                if (e.getCause() instanceof SecurityException) {
                    Log.w(TAG, "Location permission denied for removeUpdates, returning default");
                    return defaultReturn(method);
                }
                throw e;
            }
        }
    }

    @ProxyMethod("getProviderProperties")
    public static class GetProviderProperties extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(who, args);
            } catch (Exception e) {
                if (e.getCause() instanceof SecurityException) {
                    Log.w(TAG, "Location permission denied for getProviderProperties, returning null");
                    return null;
                }
                throw e;
            }
        }
    }

    @ProxyMethod("isLocationEnabledForUser")
    public static class IsLocationEnabledForUser extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return false;
        }
    }

    @ProxyMethod("isLocationEnabled")
    public static class IsLocationEnabled extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return false;
        }
    }

    @ProxyMethod("removeGpsStatusListener")
    public static class RemoveGpsStatusListener extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {

            return 0;
        }
    }

    @ProxyMethod("setExtraLocationControllerPackageEnabled")
    public static class setExtraLocationControllerPackageEnabled extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }
}
