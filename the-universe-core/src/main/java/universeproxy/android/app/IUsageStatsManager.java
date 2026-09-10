package universeproxy.android.app;

import android.os.IBinder;
import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.app.usage.IUsageStatsManager")
public interface IUsageStatsManager {
    @BClassName("android.app.usage.IUsageStatsManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
