package com.dd.the.universe.core.system.pm.installer;

import java.io.File;
import java.io.IOException;

import com.dd.the.universe.utils.BzFileUtils;

/**
 * Builds a package's binaries in a sibling staging directory and swaps it in with renames, so a
 * failed install or update never deletes the copy that other virtual users are still running.
 */
public final class PackageDirectoryTransaction {
    public static final String STAGING_SUFFIX = ".staging";
    public static final String PREVIOUS_SUFFIX = ".previous";

    private final File target;
    private final File staging;
    private final File previous;

    public PackageDirectoryTransaction(File target) {
        this.target = target;
        this.staging = new File(target.getParentFile(), target.getName() + STAGING_SUFFIX);
        this.previous = new File(target.getParentFile(), target.getName() + PREVIOUS_SUFFIX);
    }

    public static boolean isTransientDirectory(String name) {
        return name.endsWith(STAGING_SUFFIX) || name.endsWith(PREVIOUS_SUFFIX);
    }

    public File stagingDir() {
        return staging;
    }

    public File targetDir() {
        return target;
    }

    /** Restores a swap interrupted by process death and clears any abandoned staging directory. */
    public void recover() {
        if (!target.exists() && previous.exists() && !previous.renameTo(target)) {
            return;
        }
        BzFileUtils.deleteDir(staging);
        if (target.exists()) {
            BzFileUtils.deleteDir(previous);
        }
    }

    public boolean begin() {
        recover();
        if (!BzFileUtils.deleteDirFully(staging)) {
            return false;
        }
        return staging.mkdirs() || staging.isDirectory();
    }

    /**
     * Swaps staging into place. Files named in {@code carriedOver} (such as persisted settings) are
     * copied from the current directory first when staging does not provide them.
     */
    public boolean commit(String... carriedOver) {
        if (!staging.isDirectory()) {
            return false;
        }
        try {
            for (String name : carriedOver) {
                File current = new File(target, name);
                File staged = new File(staging, name);
                if (current.isFile() && !staged.exists()) {
                    BzFileUtils.copyFile(current, staged);
                }
            }
        } catch (IOException error) {
            return false;
        }
        if (!BzFileUtils.deleteDirFully(previous)) {
            return false;
        }
        if (target.exists() && !target.renameTo(previous)) {
            return false;
        }
        if (!staging.renameTo(target)) {
            previous.renameTo(target);
            return false;
        }
        BzFileUtils.deleteDir(previous);
        return true;
    }

    public void abort() {
        BzFileUtils.deleteDir(staging);
    }
}
