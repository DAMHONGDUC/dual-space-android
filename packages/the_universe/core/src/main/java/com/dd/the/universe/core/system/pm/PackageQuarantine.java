package com.dd.the.universe.core.system.pm;

import java.io.File;

/** Moves unreadable package state aside instead of deleting it, so it can still be recovered. */
final class PackageQuarantine {
    private PackageQuarantine() {
    }

    static File move(File appDir, File quarantineRoot, long nowMillis) {
        if (appDir == null || !appDir.exists()) {
            return null;
        }
        if (!quarantineRoot.isDirectory() && !quarantineRoot.mkdirs()) {
            return null;
        }
        File destination = new File(quarantineRoot, appDir.getName() + "-" + nowMillis);
        return appDir.renameTo(destination) ? destination : null;
    }
}
