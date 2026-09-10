package universeproxy.android.content.pm;

import android.os.IBinder;
import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.content.pm.IShortcutService")
public interface IShortcutService {
    @BClassName("android.content.pm.IShortcutService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
