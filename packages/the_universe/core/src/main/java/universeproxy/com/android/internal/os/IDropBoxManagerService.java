package universeproxy.com.android.internal.os;

import android.os.IBinder;
import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("com.android.internal.os.IDropBoxManagerService")
public interface IDropBoxManagerService {
    @BClassName("com.android.internal.os.IDropBoxManagerService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
