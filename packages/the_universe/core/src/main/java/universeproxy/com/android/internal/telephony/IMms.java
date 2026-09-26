package universeproxy.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("com.android.internal.telephony.IMms")
public interface IMms {
    @BClassName("com.android.internal.telephony.IMms$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
