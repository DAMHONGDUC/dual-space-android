package universeproxy.android.app.job;

import android.os.IBinder;
import android.os.PersistableBundle;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BField;

@BClassName("android.app.job.JobParameters")
public interface JobParameters {
    @BField
    IBinder callback();

    @BField
    PersistableBundle extras();

    @BField
    int jobId();
}
