package com.dd.the.universe.core.system.pm.installer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PackageDirectoryTransactionTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void abortedInstallLeavesTheRunningCopyUntouched() throws IOException {
        File target = liveDirectory("old-lib");
        PackageDirectoryTransaction transaction = new PackageDirectoryTransaction(target);

        assertTrue(transaction.begin());
        write(new File(transaction.stagingDir(), "lib/libgame.so"), "half-copied");
        transaction.abort();

        assertEquals("old-lib", read(new File(target, "lib/libgame.so")));
        assertFalse(transaction.stagingDir().exists());
    }

    @Test
    public void commitSwapsBinariesAndCarriesPackageSettingsOver() throws IOException {
        File target = liveDirectory("old-lib");
        write(new File(target, "package.conf"), "settings");
        PackageDirectoryTransaction transaction = new PackageDirectoryTransaction(target);

        assertTrue(transaction.begin());
        write(new File(transaction.stagingDir(), "lib/libgame.so"), "new-lib");
        assertTrue(transaction.commit("package.conf"));

        assertEquals("new-lib", read(new File(target, "lib/libgame.so")));
        assertEquals("settings", read(new File(target, "package.conf")));
        assertFalse(transaction.stagingDir().exists());
        assertFalse(new File(target.getParentFile(), target.getName() + PackageDirectoryTransaction.PREVIOUS_SUFFIX).exists());
    }

    @Test
    public void recoverRestoresPreviousWhenProcessDiedMidSwap() throws IOException {
        File target = new File(folder.getRoot(), "com.example.game");
        File previous = new File(folder.getRoot(), "com.example.game" + PackageDirectoryTransaction.PREVIOUS_SUFFIX);
        write(new File(previous, "lib/libgame.so"), "old-lib");
        write(new File(folder.getRoot(), "com.example.game" + PackageDirectoryTransaction.STAGING_SUFFIX + "/lib/libgame.so"), "partial");

        new PackageDirectoryTransaction(target).recover();

        assertEquals("old-lib", read(new File(target, "lib/libgame.so")));
        assertFalse(previous.exists());
        assertFalse(new PackageDirectoryTransaction(target).stagingDir().exists());
    }

    @Test
    public void commitWithoutBeginFails() throws IOException {
        File target = liveDirectory("old-lib");

        assertFalse(new PackageDirectoryTransaction(target).commit());
        assertEquals("old-lib", read(new File(target, "lib/libgame.so")));
    }

    @Test
    public void transientDirectoriesAreRecognised() {
        assertTrue(PackageDirectoryTransaction.isTransientDirectory("com.example.game.staging"));
        assertTrue(PackageDirectoryTransaction.isTransientDirectory("com.example.game.previous"));
        assertFalse(PackageDirectoryTransaction.isTransientDirectory("com.example.game"));
    }

    private File liveDirectory(String libContent) throws IOException {
        File target = new File(folder.getRoot(), "com.example.game");
        write(new File(target, "lib/libgame.so"), libContent);
        return target;
    }

    private static void write(File file, String content) throws IOException {
        file.getParentFile().mkdirs();
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    private static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
