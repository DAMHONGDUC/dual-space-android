package universeproxy.android.content;

import android.os.IBinder;
import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("android.content.IRestrictionsManager")
public interface IRestrictionsManager {
    @BClassName("android.content.IRestrictionsManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
