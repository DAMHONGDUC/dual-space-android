package com.dd.the.universe.core.system.pm.installer;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.dd.the.universe.core.system.pm.BPackageSettings;
import com.dd.the.universe.entity.pm.InstallOption;
import com.dd.the.universe.utils.BzFileUtils;
import com.dd.the.universe.utils.NativeUtils;
import com.dd.the.universe.utils.Slog;


/**
 * Copies APKs and native libraries into a staging directory while pointing the package at the
 * final directory, which only exists after {@link PackageDirectoryTransaction#commit} succeeds.
 * Any missing split or failed copy fails the whole install instead of producing a partial copy.
 */
public class CopyExecutor implements Executor {
    private static final String TAG = "CopyExecutor";
    static final String LIB_DIR = "lib";
    static final String BASE_APK = "base.apk";
    static final String SPLIT_DIR = "splits";

    private final File stagingRoot;
    private final File finalRoot;

    public CopyExecutor(File stagingRoot, File finalRoot) {
        this.stagingRoot = stagingRoot;
        this.finalRoot = finalRoot;
    }

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String[] splits = ps.pkg.applicationInfo != null ? ps.pkg.applicationInfo.splitSourceDirs : null;
        try {
            if (!option.isFlag(InstallOption.FLAG_SYSTEM)) {
                File libDir = new File(stagingRoot, LIB_DIR);
                NativeUtils.copyNativeLib(new File(ps.pkg.baseCodePath), libDir);
                if (splits != null) {
                    for (String split : splits) {
                        if (split == null || split.length() == 0) {
                            continue;
                        }
                        NativeUtils.copyNativeLib(requireFile(split), libDir);
                    }
                }
            }
            if (option.isFlag(InstallOption.FLAG_STORAGE)) {
                File origFile = new File(ps.pkg.baseCodePath);
                File stagedBase = new File(stagingRoot, BASE_APK);
                if (!option.isFlag(InstallOption.FLAG_URI_FILE) || !BzFileUtils.renameTo(origFile, stagedBase)) {
                    BzFileUtils.copyFile(requireFile(origFile.getAbsolutePath()), stagedBase);
                }
                stagedBase.setReadOnly();
                ps.pkg.baseCodePath = new File(finalRoot, BASE_APK).getAbsolutePath();

                if (splits != null && splits.length > 0) {
                    File stagedSplitDir = new File(stagingRoot, SPLIT_DIR);
                    BzFileUtils.mkdirs(stagedSplitDir);
                    ArrayList<String> finalSplitPaths = new ArrayList<>();
                    for (String split : splits) {
                        if (split == null || split.length() == 0) {
                            continue;
                        }
                        File splitFile = requireFile(split);
                        File copied = new File(stagedSplitDir, splitFile.getName());
                        BzFileUtils.copyFile(splitFile, copied);
                        copied.setReadOnly();
                        finalSplitPaths.add(new File(new File(finalRoot, SPLIT_DIR), splitFile.getName()).getAbsolutePath());
                    }
                    ps.pkg.applicationInfo.splitSourceDirs = finalSplitPaths.toArray(new String[0]);
                    ps.pkg.applicationInfo.splitPublicSourceDirs = ps.pkg.applicationInfo.splitSourceDirs;
                    Slog.i(TAG, "copied splits: pkg=" + ps.pkg.packageName + " count=" + finalSplitPaths.size());
                }
            }
            return 0;
        } catch (Exception e) {
            Slog.e(TAG, "copy failed: pkg=" + ps.pkg.packageName, e);
            return -1;
        }
    }

    private static File requireFile(String path) throws IOException {
        File file = new File(path);
        if (!file.isFile()) {
            throw new IOException("missing install file: " + file.getName());
        }
        return file;
    }
}
