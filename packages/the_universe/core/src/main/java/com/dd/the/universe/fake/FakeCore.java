package com.dd.the.universe.fake;

import com.dd.the.universe.ReflectCore;


public class FakeCore {
    public static void init() {
        ReflectCore.set(android.app.ActivityThread.class);
    }
}
