package universeproxy.com.android.internal.app;

import android.os.IBinder;
import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("com.android.internal.app.IBatteryStats")
public interface IBatteryStats {
    @BClassName("com.android.internal.app.IBatteryStats$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
