package universeproxy.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import com.duplicateapp.theuniverse.reflection.annotation.BClassName;
import com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod;

@BClassName("com.android.internal.telephony.ISub")
public interface ISub {
    @BClassName("com.android.internal.telephony.ISub$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
