package universeproxy.com.android.internal.telecom;

import android.os.IBinder;
import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("com.android.internal.telecom.ITelecomService")
public interface ITelecomService {
    @BClassName("com.android.internal.telecom.ITelecomService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
