package com.duplicateapp.theuniverse.fake.service;

import android.os.IBinder;

import universeproxy.android.os.BRServiceManager;
import universeproxy.android.view.BRIGraphicsStatsStub;
import com.duplicateapp.theuniverse.fake.hook.BinderInvocationStub;
import com.duplicateapp.theuniverse.fake.service.base.PkgMethodProxy;


public class IFingerprintManagerProxy extends BinderInvocationStub {
    // Modified for Parallel_app: Android API 37 no longer exposes Context.FINGERPRINT_SERVICE.
    private static final String FINGERPRINT_SERVICE = "fingerprint";

    public IFingerprintManagerProxy() {
        super(BRServiceManager.get().getService(FINGERPRINT_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIGraphicsStatsStub.get().asInterface(BRServiceManager.get().getService(FINGERPRINT_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(FINGERPRINT_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new PkgMethodProxy("isHardwareDetected"));
        addMethodHook(new PkgMethodProxy("hasEnrolledFingerprints"));
        addMethodHook(new PkgMethodProxy("authenticate"));
        addMethodHook(new PkgMethodProxy("cancelAuthentication"));
        addMethodHook(new PkgMethodProxy("getEnrolledFingerprints"));
        addMethodHook(new PkgMethodProxy("getAuthenticatorId"));
    }
}
