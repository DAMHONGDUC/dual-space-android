package universeproxy.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("com.android.internal.telephony.ITelephonyRegistry")
public interface ITelephonyRegistry {
    @BClassName("com.android.internal.telephony.ITelephonyRegistry$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
