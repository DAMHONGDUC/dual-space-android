package universeproxy.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("com.android.internal.telephony.ISms")
public interface ISms {
    @BClassName("com.android.internal.telephony.ISms$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
