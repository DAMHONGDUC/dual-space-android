package universeproxy.android.app;

import android.os.IBinder;
import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.app.IUiModeManager")
public interface IUiModeManager {
    @BClassName("android.app.IUiModeManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
