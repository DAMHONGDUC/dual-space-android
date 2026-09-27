package com.dd.the.universe.utils;

import java.lang.reflect.Method;
import org.junit.Test;
import static org.junit.Assert.assertSame;

public class SimpleCrashFixTest {
    @Test public void preservesPlatformCrashHandler() throws Exception {
        Thread.UncaughtExceptionHandler original = Thread.getDefaultUncaughtExceptionHandler();
        Thread.UncaughtExceptionHandler sentinel = (thread, error) -> {};
        try {
            Thread.setDefaultUncaughtExceptionHandler(sentinel);
            Method install = SimpleCrashFix.class.getDeclaredMethod("installGlobalExceptionHandler");
            install.setAccessible(true);
            install.invoke(null);
            assertSame(sentinel, Thread.getDefaultUncaughtExceptionHandler());
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(original);
        }
    }
}
