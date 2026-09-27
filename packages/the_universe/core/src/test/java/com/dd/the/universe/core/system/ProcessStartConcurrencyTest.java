package com.dd.the.universe.core.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.pm.ApplicationInfo;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

public class ProcessStartConcurrencyTest {
    private static final String PROCESS = "com.example.game";
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    @After
    public void tearDown() {
        executor.shutdownNow();
    }

    @Test
    public void hungInitializationDoesNotBlockRegistryLookups() throws Exception {
        FakeManager manager = new FakeManager();
        Future<ProcessRecord> start = executor.submit(() -> manager.startProcess(new ApplicationInfo(), PROCESS, 1, 10001, -1, 0));
        assertTrue(manager.initEntered.await(2, TimeUnit.SECONDS));

        Future<ProcessRecord> lookup = executor.submit(() -> manager.findProcessByPid(4242));

        assertNull(lookup.get(1, TimeUnit.SECONDS));
        manager.releaseInit.countDown();
        assertNotNull(start.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void concurrentStartsShareOneProcessInsteadOfDoubleAllocating() throws Exception {
        FakeManager manager = new FakeManager();
        Future<ProcessRecord> first = executor.submit(() -> manager.startProcess(new ApplicationInfo(), PROCESS, 1, 10001, -1, 0));
        assertTrue(manager.initEntered.await(2, TimeUnit.SECONDS));
        Future<ProcessRecord> second = executor.submit(() -> manager.startProcess(new ApplicationInfo(), PROCESS, 1, 10001, -1, 0));

        Thread.sleep(200);
        manager.releaseInit.countDown();

        ProcessRecord firstRecord = first.get(2, TimeUnit.SECONDS);
        assertSame(firstRecord, second.get(2, TimeUnit.SECONDS));
        assertEquals(1, manager.initCalls.get());
    }

    @Test
    public void failedInitializationFreesTheSlotAndWakesWaiters() throws Exception {
        FakeManager manager = new FakeManager();
        manager.initResult = false;
        manager.releaseInit.countDown();

        assertNull(manager.startProcess(new ApplicationInfo(), PROCESS, 1, 10001, -1, 0));
        assertTrue(manager.getPackageProcessRecordsForTest().isEmpty());

        manager.initResult = true;
        assertNotNull(manager.startProcess(new ApplicationInfo(), PROCESS, 1, 10001, -1, 0));
    }

    @Test
    public void waiterGivesUpAfterTimeoutWhenInitializationHangs() throws Exception {
        FakeManager manager = new FakeManager();
        manager.timeoutMillis = 100;
        executor.submit(() -> manager.startProcess(new ApplicationInfo(), PROCESS, 1, 10001, -1, 0));
        assertTrue(manager.initEntered.await(2, TimeUnit.SECONDS));

        long startedAt = System.nanoTime();
        assertNull(manager.startProcess(new ApplicationInfo(), PROCESS, 1, 10001, -1, 0));
        assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt) < 2_000);
        manager.releaseInit.countDown();
    }

    private static class FakeManager extends BProcessManagerService {
        final CountDownLatch initEntered = new CountDownLatch(1);
        final CountDownLatch releaseInit = new CountDownLatch(1);
        final AtomicInteger initCalls = new AtomicInteger();
        final Set<ProcessRecord> attached = Collections.synchronizedSet(Collections.newSetFromMap(new java.util.IdentityHashMap<>()));
        volatile boolean initResult = true;
        volatile long timeoutMillis = 5_000;
        private int nextBPid;

        @Override
        boolean initAppProcess(ProcessRecord record) {
            initCalls.incrementAndGet();
            initEntered.countDown();
            try {
                releaseInit.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                return false;
            }
            if (initResult) {
                attached.add(record);
            }
            return initResult;
        }

        @Override
        boolean isAttached(ProcessRecord app) {
            return attached.contains(app);
        }

        @Override
        long initTimeoutMillis() {
            return timeoutMillis;
        }

        @Override
        synchronized int getUsingBPidL() {
            return nextBPid++;
        }

        @Override
        int resolvePid(ProcessRecord app) {
            return 1000 + app.bpid;
        }

        java.util.List<ProcessRecord> getPackageProcessRecordsForTest() throws Exception {
            java.lang.reflect.Field field = BProcessManagerService.class.getDeclaredField("mPidsSelfLocked");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<ProcessRecord> records = (java.util.List<ProcessRecord>) field.get(this);
            return records;
        }
    }
}
