package universeproxy.android.view.accessibility;

import android.os.IBinder;
import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.view.accessibility.IAccessibilityManager")
public interface IAccessibilityManager {
    @BClassName("android.view.accessibility.IAccessibilityManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
