package universeproxy.android.app.job;

import android.os.IBinder;
import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.app.job.IJobScheduler")
public interface IJobScheduler {
    @BClassName("android.app.job.IJobScheduler$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
