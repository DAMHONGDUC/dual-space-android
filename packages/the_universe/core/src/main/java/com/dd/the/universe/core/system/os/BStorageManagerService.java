package com.dd.the.universe.core.system.os;

import android.net.Uri;
import android.os.Process;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.content.Context;

import java.io.File;
import java.util.List;

import universeproxy.android.os.storage.BRStorageManager;
import universeproxy.android.os.storage.BRStorageVolume;
import com.dd.the.universe.TheUniverseCore;
import com.dd.the.universe.core.env.BEnvironment;
import com.dd.the.universe.core.system.ISystemService;
import com.dd.the.universe.core.system.user.BUserHandle;
import com.dd.the.universe.fake.provider.FileProvider;
import com.dd.the.universe.proxy.ProxyManifest;
import com.dd.the.universe.utils.compat.BuildCompat;


public class BStorageManagerService extends IBStorageManagerService.Stub implements ISystemService {
    private static final BStorageManagerService sService = new BStorageManagerService();

    public static BStorageManagerService get() {
        return sService;
    }

    public BStorageManagerService() {
    }

    @Override
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags, int userId) throws RemoteException {
        StorageVolume[] storageVolumes = null;
        try {
            if (BRStorageManager.get().getVolumeList(0, 0) != null) {
                storageVolumes = BRStorageManager.get().getVolumeList(BUserHandle.getUserId(Process.myUid()), 0);
            }
        } catch (Throwable ignored) {
        }

        if (storageVolumes == null) {
            try {
                Context context = TheUniverseCore.getContext();
                StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                if (storageManager != null) {
                    List<StorageVolume> volumes = storageManager.getStorageVolumes();
                    if (volumes != null) {
                        storageVolumes = volumes.toArray(new StorageVolume[0]);
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        if (storageVolumes == null) {
            return new StorageVolume[]{};
        }

        try {
            for (StorageVolume storageVolume : storageVolumes) {
                BRStorageVolume.get(storageVolume)._set_mPath(BEnvironment.getExternalUserDir(userId));
                if (BuildCompat.isPie()) {
                    BRStorageVolume.get(storageVolume)._set_mInternalPath(BEnvironment.getExternalUserDir(userId));
                }
            }
        } catch (Throwable ignored) {
        }
        return storageVolumes;
    }

    @Override
    public Uri getUriForFile(String file) throws RemoteException {
        return FileProvider.getUriForFile(TheUniverseCore.getContext(), ProxyManifest.getProxyFileProvider(), new File(file));
    }

    @Override
    public void systemReady() {

    }
}
