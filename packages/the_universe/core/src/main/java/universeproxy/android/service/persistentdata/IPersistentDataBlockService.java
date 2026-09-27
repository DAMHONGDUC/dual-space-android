package universeproxy.android.service.persistentdata;

import android.os.IBinder;
import android.os.IInterface;

import com.dd.the.universe.reflection.annotation.BClassName;
import com.dd.the.universe.reflection.annotation.BStaticMethod;

@BClassName("android.service.persistentdata.IPersistentDataBlockService")
public interface IPersistentDataBlockService {
    @BClassName("android.service.persistentdata.IPersistentDataBlockService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
