package com.dd.the.universe.fake.frameworks;

import android.os.IBinder;
import android.os.IInterface;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TheUniverseManagerTest {
    @Test
    public void cachedServiceHealthDoesNotPingBinderAndClearInvalidatesIt() throws Exception {
        IBinder binder = (IBinder) Proxy.newProxyInstance(IBinder.class.getClassLoader(),
                new Class<?>[]{IBinder.class}, (proxy, method, args) -> {
                    if (method.getName().equals("pingBinder")) {
                        throw new AssertionError("Health checks must not perform synchronous IPC");
                    }
                    if (method.getName().equals("isBinderAlive")) return true;
                    return null;
                });
        IInterface service = () -> binder;
        TheUniverseManager<IInterface> manager = new TheUniverseManager<IInterface>() {
            @Override
            protected String getServiceName() {
                return "test";
            }
        };
        Field field = TheUniverseManager.class.getDeclaredField("mService");
        field.setAccessible(true);
        field.set(manager, service);

        assertSame(service, manager.getService());
        assertTrue(manager.isServiceHealthy());
        manager.clearServiceCache();
        assertFalse(manager.isServiceHealthy());
    }
}
