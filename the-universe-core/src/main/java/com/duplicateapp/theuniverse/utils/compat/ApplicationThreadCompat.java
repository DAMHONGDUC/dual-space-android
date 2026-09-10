package com.duplicateapp.theuniverse.utils.compat;

import android.os.IBinder;
import android.os.IInterface;

import universeproxy.android.app.BRApplicationThreadNative;
import universeproxy.android.app.BRIApplicationThreadOreoStub;

public class ApplicationThreadCompat {

    public static IInterface asInterface(IBinder binder) {
        if (BuildCompat.isOreo()) {
            return BRIApplicationThreadOreoStub.get().asInterface(binder);
        }
        return BRApplicationThreadNative.get().asInterface(binder);
    }
}
