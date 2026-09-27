package com.dd.the.universe.core.system.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PackageRecoveryTest {
    private static final int FIRST_APPLICATION_UID = 10000;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void unreadablePackageIsMovedAsideNotDeleted() throws IOException {
        File appDir = folder.newFolder("apps", "com.example.game");
        assertTrue(new File(appDir, "package.conf").createNewFile());
        File quarantineRoot = new File(folder.getRoot(), "quarantine");

        File moved = PackageQuarantine.move(appDir, quarantineRoot, 42L);

        assertNotNull(moved);
        assertFalse(appDir.exists());
        assertTrue(new File(moved, "package.conf").exists());
        assertEquals("com.example.game-42", moved.getName());
    }

    @Test
    public void missingPackageIsNotQuarantined() {
        assertNull(PackageQuarantine.move(new File(folder.getRoot(), "missing"), folder.getRoot(), 1L));
    }

    @Test
    public void rebuiltUidTableSkipsIdsAlreadyOwnedByPackages() {
        int floor = AppIdRecovery.nextOffsetFloor(0, Arrays.asList(10003, 10007, 10001), FIRST_APPLICATION_UID);

        assertEquals(7, floor);
    }

    @Test
    public void allocatorNeverMovesBackwards() {
        assertEquals(12, AppIdRecovery.nextOffsetFloor(12, Arrays.asList(10003, null, 999), FIRST_APPLICATION_UID));
    }
}
