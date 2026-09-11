package com.duplicateapp.theuniverse.fake.service;

import android.app.job.JobInfo;
import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

import universeproxy.android.app.job.BRIJobSchedulerStub;
import universeproxy.android.os.BRServiceManager;
import com.duplicateapp.theuniverse.TheUniverseCore;
import com.duplicateapp.theuniverse.app.BActivityThread;
import com.duplicateapp.theuniverse.fake.hook.BinderInvocationStub;
import com.duplicateapp.theuniverse.fake.hook.MethodHook;
import com.duplicateapp.theuniverse.fake.hook.ProxyMethod;
import com.duplicateapp.theuniverse.utils.Slog;
import com.duplicateapp.theuniverse.utils.UIDSpoofingHelper;


public class IJobServiceProxy extends BinderInvocationStub {
    public static final String TAG = "JobServiceStub";

    private static int findJobInfoArgIndex(Object[] args) {
        if (args == null) return -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof JobInfo) return i;
        }
        return -1;
    }

    public IJobServiceProxy() {
        super(BRServiceManager.get().getService(Context.JOB_SCHEDULER_SERVICE));
    }

    @Override
    protected Object getWho() {
        IBinder jobScheduler = BRServiceManager.get().getService("jobscheduler");
        return BRIJobSchedulerStub.get().asInterface(jobScheduler);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @ProxyMethod("schedule")
    public static class Schedule extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (args == null || args.length == 0) {
                    Slog.w(TAG, "Schedule: No arguments provided, returning RESULT_FAILURE");
                    return 0; 
                }

                final int jobInfoIndex = findJobInfoArgIndex(args);
                if (jobInfoIndex < 0) {
                    Slog.w(TAG, "Schedule: No JobInfo found in args, invoking original");
                    return method.invoke(who, args);
                }

                JobInfo jobInfo = (JobInfo) args[jobInfoIndex];
                if (jobInfo == null) {
                    Slog.w(TAG, "Schedule: JobInfo is null (idx=" + jobInfoIndex + "), returning RESULT_FAILURE");
                    return 0;
                }

                Slog.d(TAG, "Schedule: Processing JobInfo for package: " + jobInfo.getService().getPackageName() + " (idx=" + jobInfoIndex + ")");
                
                
                try {
                    JobInfo proxyJobInfo = TheUniverseCore.getBJobManager().schedule(jobInfo);
                    if (proxyJobInfo != null) {
                        args[jobInfoIndex] = proxyJobInfo;
                        Slog.d(TAG, "Schedule: Successfully created proxy JobInfo");
                        return method.invoke(who, args);
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "Schedule: TheUniverse job manager failed, trying system fallback", e);
                }

                return method.invoke(who, args);
                
            } catch (Exception e) {
                Slog.e(TAG, "Schedule: Error processing job", e);
                
                
                if (isUIDValidationError(e)) {
                    Slog.w(TAG, "UID validation failed for job scheduling, returning RESULT_FAILURE: " + e.getCause().getMessage());
                    return 0; 
                }
                try {
                    return method.invoke(who, args);
                } catch (Exception fallbackException) {
                    Slog.e(TAG, "Schedule: Fallback also failed", fallbackException);
                    return 0; 
                }
            }
        }
        
        
        private boolean isUIDValidationError(Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                String message = e.getCause().getMessage();
                return message != null && message.contains("cannot schedule job");
            }
            
            if (e.getCause() instanceof android.os.RemoteException) {
                String message = e.getCause().getMessage();
                return message != null && message.contains("cannot schedule job");
            }
            
            return false;
        }
    }

    @ProxyMethod("cancel")
    public static class Cancel extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Integer jobId = (Integer) args[0];
                if (jobId == null) {
                    Slog.w(TAG, "Cancel: JobId is null");
                    return method.invoke(who, args);
                }
                
                args[0] = TheUniverseCore.getBJobManager()
                        .cancel(BActivityThread.getAppConfig().processName, jobId);
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.e(TAG, "Cancel: Error canceling job", e);
                return method.invoke(who, args);
            }
        }
    }

    @ProxyMethod("cancelAll")
    public static class CancelAll extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                TheUniverseCore.getBJobManager().cancelAll(BActivityThread.getAppConfig().processName);
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.e(TAG, "CancelAll: Error canceling all jobs", e);
                return method.invoke(who, args);
            }
        }
    }

    @ProxyMethod("enqueue")
    public static class Enqueue extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (args == null || args.length == 0) {
                    Slog.w(TAG, "Enqueue: No arguments provided, returning RESULT_FAILURE");
                    return 0; 
                }

                final int jobInfoIndex = findJobInfoArgIndex(args);
                if (jobInfoIndex < 0) {
                    Slog.w(TAG, "Enqueue: No JobInfo found in args, invoking original");
                    return method.invoke(who, args);
                }

                JobInfo jobInfo = (JobInfo) args[jobInfoIndex];
                if (jobInfo == null) {
                    Slog.w(TAG, "Enqueue: JobInfo is null (idx=" + jobInfoIndex + "), returning RESULT_FAILURE");
                    return 0;
                }

                Slog.d(TAG, "Enqueue: Processing JobInfo for package: " + jobInfo.getService().getPackageName() + " (idx=" + jobInfoIndex + ")");
                
                
                try {
                    JobInfo proxyJobInfo = TheUniverseCore.getBJobManager().schedule(jobInfo);
                    if (proxyJobInfo != null) {
                        args[jobInfoIndex] = proxyJobInfo;
                        Slog.d(TAG, "Enqueue: Successfully created proxy JobInfo");
                        return method.invoke(who, args);
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "Enqueue: TheUniverse job manager failed, trying system fallback", e);
                }

                return method.invoke(who, args);
                
            } catch (Exception e) {
                Slog.e(TAG, "Enqueue: Error processing job", e);
                
                
                if (isUIDValidationError(e)) {
                    Slog.w(TAG, "UID validation failed for job enqueuing, returning RESULT_FAILURE: " + e.getCause().getMessage());
                    return 0; 
                }
                try {
                    return method.invoke(who, args);
                } catch (Exception fallbackException) {
                    Slog.e(TAG, "Enqueue: Fallback also failed", fallbackException);
                    return 0; 
                }
            }
        }
        
        
        private boolean isUIDValidationError(Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                String message = e.getCause().getMessage();
                return message != null && message.contains("cannot schedule job");
            }
            
            if (e.getCause() instanceof android.os.RemoteException) {
                String message = e.getCause().getMessage();
                return message != null && message.contains("cannot schedule job");
            }
            
            return false;
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
