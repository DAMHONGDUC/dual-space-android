package com.dd.the.universe.fake.frameworks;

import android.app.job.JobInfo;
import android.os.RemoteException;

import com.dd.the.universe.app.BActivityThread;
import com.dd.the.universe.core.system.ServiceManager;
import com.dd.the.universe.core.system.am.IBJobManagerService;
import com.dd.the.universe.entity.JobRecord;


public class BJobManager extends TheUniverseManager<IBJobManagerService> {
    private static final BJobManager sJobManager = new BJobManager();

    public static BJobManager get() {
        return sJobManager;
    }

    @Override
    protected String getServiceName() {
        return ServiceManager.JOB_MANAGER;
    }

    public JobInfo schedule(JobInfo info) {
        try {
            return getService().schedule(info, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JobRecord queryJobRecord(String processName, int jobId) {
        try {
            return getService().queryJobRecord(processName, jobId, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cancelAll(String processName) {
        try {
            getService().cancelAll(processName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int cancel(String processName, int jobId) {
        try {
            return getService().cancel(processName, jobId, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
