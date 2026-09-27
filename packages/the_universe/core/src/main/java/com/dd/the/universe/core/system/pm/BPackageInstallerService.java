package com.dd.the.universe.core.system.pm;

import java.util.ArrayList;
import java.util.List;

import com.dd.the.universe.core.system.ISystemService;
import com.dd.the.universe.core.system.pm.installer.CopyExecutor;
import com.dd.the.universe.core.system.pm.installer.PackageDirectoryTransaction;
import com.dd.the.universe.core.env.BEnvironment;
import com.dd.the.universe.core.system.pm.installer.CreateUserExecutor;
import com.dd.the.universe.core.system.pm.installer.Executor;
import com.dd.the.universe.core.system.pm.installer.RemoveAppExecutor;
import com.dd.the.universe.core.system.pm.installer.RemoveUserExecutor;
import com.dd.the.universe.entity.pm.InstallOption;
import com.dd.the.universe.utils.Slog;


public class BPackageInstallerService extends IBPackageInstallerService.Stub implements ISystemService {
    private static final BPackageInstallerService sService = new BPackageInstallerService();

    public static BPackageInstallerService get() {
        return sService;
    }

    public static final String TAG = "BPackageInstallerService";

    @Override
    public int installPackageAsUser(BPackageSettings ps, int userId) {
        int created = new CreateUserExecutor().exec(ps, ps.installOption, userId);
        Slog.d(TAG, "installPackageAsUser: CreateUserExecutor exec: " + created);
        if (created != 0) {
            return created;
        }
        return installPackageFiles(ps, userId);
    }

    // Stages binaries beside the live directory and swaps them in only after every copy succeeded.
    private int installPackageFiles(BPackageSettings ps, int userId) {
        PackageDirectoryTransaction transaction = new PackageDirectoryTransaction(BEnvironment.getAppDir(ps.pkg.packageName));
        if (!transaction.begin()) {
            Slog.e(TAG, "installPackageFiles: cannot prepare staging for " + ps.pkg.packageName);
            return -1;
        }
        try {
            int copied = new CopyExecutor(transaction.stagingDir(), transaction.targetDir()).exec(ps, ps.installOption, userId);
            if (copied != 0) {
                transaction.abort();
                return copied;
            }
            if (!transaction.commit(BEnvironment.getPackageConf(ps.pkg.packageName).getName())) {
                Slog.e(TAG, "installPackageFiles: commit failed for " + ps.pkg.packageName);
                transaction.abort();
                return -1;
            }
            return 0;
        } catch (Throwable error) {
            Slog.e(TAG, "installPackageFiles: " + ps.pkg.packageName, error);
            transaction.abort();
            return -1;
        }
    }

    @Override
    public int uninstallPackageAsUser(BPackageSettings ps, boolean removeApp, int userId) {
        InstallOption option = ps.installOption;
        // User data goes first: if it cannot be removed, the binaries stay and the copy remains usable.
        int removedUser = new RemoveUserExecutor().exec(ps, option, userId);
        Slog.d(TAG, "uninstallPackageAsUser: RemoveUserExecutor exec: " + removedUser);
        if (removedUser != 0 || !removeApp) {
            return removedUser;
        }
        // Leftover binaries hold no user data, so they are logged rather than blocking the uninstall.
        int removedApp = new RemoveAppExecutor().exec(ps, option, userId);
        Slog.d(TAG, "uninstallPackageAsUser: RemoveAppExecutor exec: " + removedApp);
        return 0;
    }

    @Override
    public int clearPackage(BPackageSettings ps, int userId) {
        List<Executor> executors = new ArrayList<>();
        
        executors.add(new RemoveUserExecutor());
        
        executors.add(new CreateUserExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, userId);
            Slog.d(TAG, "uninstallPackageAsUser: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    @Override
    public int updatePackage(BPackageSettings ps) {
        return installPackageFiles(ps, -1);
    }

    @Override
    public void systemReady() {

    }
}
