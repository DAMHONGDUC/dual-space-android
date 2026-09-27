package com.dd.the.universe.core.system;

import android.content.pm.ApplicationInfo;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class BProcessManagerServiceTest {
    @Test
    public void staleDeathDoesNotKillOrRemoveReplacement() throws Exception {
        BProcessManagerService manager = new BProcessManagerService();
        CountingRecord stale = new CountingRecord();
        CountingRecord replacement = new CountingRecord();
        Field field = BProcessManagerService.class.getDeclaredField("mProcessMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, Map<String, ProcessRecord>> processes =
                (Map<Integer, Map<String, ProcessRecord>>) field.get(manager);
        Map<String, ProcessRecord> userProcesses = new HashMap<>();
        userProcesses.put(stale.processName, replacement);
        processes.put(0, userProcesses);

        manager.onProcessDie(stale);

        assertEquals(0, stale.killCount);
        assertSame(replacement, userProcesses.get(stale.processName));
    }

    private static class CountingRecord extends ProcessRecord {
        int killCount;

        CountingRecord() {
            super(new ApplicationInfo(), "test.process");
        }

        @Override
        public void kill() {
            killCount++;
        }
    }
}
