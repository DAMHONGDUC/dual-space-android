package com.duplicateapp.theuniverse.fake;

import top.niunaijun.jnihook.ReflectCore;


public class FakeCore {
    public static void init() {
        ReflectCore.set(android.app.ActivityThread.class);
    }
}
